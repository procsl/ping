package cn.procsl.ping.boot.ai.adapter.cli;

import cn.procsl.ping.boot.ai.config.ConfigRoot;
import cn.procsl.ping.boot.ai.config.GlobalConfig;
import cn.procsl.ping.boot.ai.config.GlobalConfigLoader;
import cn.procsl.ping.boot.ai.config.RoleCatalog;
import cn.procsl.ping.boot.ai.config.RoleConfig;
import cn.procsl.ping.boot.ai.core.ChatClientFactory;
import cn.procsl.ping.boot.ai.core.ImageStore;
import cn.procsl.ping.boot.ai.core.ModelCatalog;
import cn.procsl.ping.boot.ai.core.ResolvedModel;
import cn.procsl.ping.boot.ai.core.Session;
import cn.procsl.ping.boot.ai.hook.HookChain;
import cn.procsl.ping.boot.ai.hook.HookContext;
import cn.procsl.ping.boot.ai.hook.ToolWhitelistHook;
import cn.procsl.ping.boot.ai.session.RoleSessionStore;
import cn.procsl.ping.boot.ai.session.SessionFile;
import cn.procsl.ping.boot.ai.session.SessionIds;
import cn.procsl.ping.boot.ai.tool.BuiltinTools;
import cn.procsl.ping.boot.ai.tool.HookedToolCallback;
import cn.procsl.ping.boot.ai.tool.ToolDescriptor;
import cn.procsl.ping.boot.ai.tool.ToolRegistry;
import cn.procsl.ping.boot.ai.tool.ToolResolver;
import cn.procsl.ping.boot.ai.tool.ToolTruncator;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.chat.client.ChatClient;

import java.io.PrintStream;
import java.time.Duration;
import java.util.List;

/**
 * 非交互式命令：解析参数 → 初始化配置 → 加载角色 → 组装模型 → 提交并渲染。
 *
 * <p>退出码：{@code 0} 成功；{@code 2} 参数错误；{@code 3} 角色不存在；
 * {@code 4} 配置不可读；{@code 5} 模型调用失败。</p>
 */
public final class CliCommand {

    /** 退出码：参数错误。 */
    public static final int EXIT_ARGUMENT = 2;
    /** 退出码：角色不存在。 */
    public static final int EXIT_ROLE = 3;
    /** 退出码：配置不可读。 */
    public static final int EXIT_CONFIG = 4;

    private static final Duration ROUND_TIMEOUT = Duration.ofMinutes(10);

    private final PrintStream out;
    private final PrintStream err;
    private final java.util.function.Function<ResolvedModel, ChatClient> clientFactory;

    public CliCommand(PrintStream out, PrintStream err) {
        this(out, err, new ChatClientFactory()::create);
    }

    CliCommand(PrintStream out, PrintStream err,
               java.util.function.Function<ResolvedModel, ChatClient> clientFactory) {
        this.out = out;
        this.err = err;
        this.clientFactory = clientFactory;
    }

    /**
     * 执行一次非交互式问答。
     *
     * @param args 命令行参数
     * @return 退出码
     */
    public int execute(CliArguments args) {
        List<String> errors = args.validate();
        if (!errors.isEmpty()) {
            errors.forEach(err::println);
            return EXIT_ARGUMENT;
        }

        ConfigRoot root = ConfigRoot.resolve(args.config()).initialize();
        GlobalConfig config = new GlobalConfigLoader().load(root.configFile());
        RoleCatalog catalog = new RoleCatalog(root.roles());
        RoleConfig role = catalog.find(args.role()).orElse(null);
        if (role == null) {
            err.println("角色不存在: " + args.role());
            err.println("可用角色: " + String.join(", ", catalog.list()));
            return EXIT_ROLE;
        }

        try {
            String alias = args.model() == null || args.model().isBlank() ? role.model() : args.model();
            ResolvedModel model = new ModelCatalog(config).resolve(alias);
            ChatClient chatClient = clientFactory.apply(model);

            String prompt = args.prompt() == null || args.prompt().isBlank() ? role.firstPrompt() : args.prompt();
            if (prompt == null || prompt.isBlank()) {
                err.println("未提供提示词，且角色没有默认用户提示词");
                return EXIT_ARGUMENT;
            }

            String sessionId = args.session() == null || args.session().isBlank()
                    ? newSessionId() : args.session();
            RoleSessionStore store = new RoleSessionStore(role.directory());
            SessionFile history = store.load(sessionId).orElse(null);
            Session session = new Session(sessionId, role.name(), model.alias(), chatClient,
                    role.systemPrompt(), null, store, new ImageStore(root.images()),
                    config.chat().maxHistory(), history);

            ToolRegistry registry = BuiltinTools.registry();
            List<ToolDescriptor> tools = new ToolResolver(registry).resolve(args.tools(), role.tools());
            HookChain hooks = new HookChain(List.of(new ToolWhitelistHook(
                    tools.stream().map(ToolDescriptor::name).collect(java.util.stream.Collectors.toSet()))));
            HookContext hookContext = new HookContext(sessionId, role.name());
            List<ToolCallback> callbacks = tools.stream()
                    .map(tool -> (ToolCallback) new HookedToolCallback(registry.find(tool.name()), tool.name(),
                            hooks, hookContext, session, new ToolTruncator()))
                    .toList();
            session.withTools(callbacks);

            return new CliAdapter(out, err, args.verbose())
                    .run(session, prompt, args.images(), ROUND_TIMEOUT);
        } catch (IllegalStateException e) {
            err.println("配置不可用: " + e.getMessage());
            return EXIT_CONFIG;
        }
    }

    /**
     * 生成会话标识：毫秒级时间戳 + 10 位大写随机字母。
     *
     * @return 会话标识
     */
    static String newSessionId() {
        return SessionIds.generate();
    }
}
