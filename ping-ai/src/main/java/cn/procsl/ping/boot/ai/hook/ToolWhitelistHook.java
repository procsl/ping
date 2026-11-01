package cn.procsl.ping.boot.ai.hook;

import tools.jackson.databind.ObjectMapper;

import java.util.Set;

/**
 * 工具白名单 hook：请求前校验工具名与参数，执行完成后校验结果归属。
 *
 * <p>绑定进模型上下文的工具集就是白名单，不另设第二份配置。</p>
 */
public final class ToolWhitelistHook implements Hook {

    /** 工具调用前阶段。 */
    public static final String TOOL_BEFORE = "tool.before";
    /** 工具调用后阶段。 */
    public static final String TOOL_AFTER = "tool.after";

    private final Set<String> allowed;
    private final ObjectMapper mapper = new ObjectMapper();

    public ToolWhitelistHook(Set<String> allowed) {
        this.allowed = Set.copyOf(allowed);
    }

    @Override
    public HookResult apply(String stage, HookContext ctx, Object data) {
        if (TOOL_BEFORE.equals(stage) && data instanceof ToolCallData call) {
            if (!allowed.contains(call.name())) {
                return HookResult.reject("工具不在白名单内: " + call.name());
            }
            if (!validJson(call.arguments())) {
                return HookResult.reject("工具参数不是合法 JSON: " + call.name());
            }
            return HookResult.pass(data);
        }
        if (TOOL_AFTER.equals(stage) && data instanceof ToolResultData result) {
            if (!allowed.contains(result.name())) {
                return HookResult.reject("结果归属未知工具: " + result.name());
            }
            if (result.result() == null) {
                return HookResult.reject("工具结果为空: " + result.name());
            }
            return HookResult.pass(data);
        }
        return HookResult.pass(data);
    }

    private boolean validJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        try {
            mapper.readTree(raw);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 工具调用数据。
     *
     * @param name      工具名
     * @param arguments 原始参数
     */
    public record ToolCallData(String name, String arguments) {
    }

    /**
     * 工具结果数据。
     *
     * @param name   工具名
     * @param result 结果
     */
    public record ToolResultData(String name, String result) {
    }
}
