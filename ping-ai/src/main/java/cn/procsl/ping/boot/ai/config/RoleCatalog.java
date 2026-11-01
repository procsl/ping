package cn.procsl.ping.boot.ai.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 角色目录读取与校验。
 *
 * <p>角色名限定 {@code ^[a-zA-Z0-9_-]{1,36}$}，仅是本地组织用的名字，不传给模型。
 * 缺失的提示词文件与损坏的 {@code role.json} 都只降级告警，不中断启动。</p>
 */
public final class RoleCatalog {

    private static final Logger log = LoggerFactory.getLogger(RoleCatalog.class);

    /** 角色名合法模式。 */
    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,36}$");

    private static final String ROLE_FILE = "role.json";
    private static final String SYSTEM_PROMPT = "system.md";
    private static final String FIRST_PROMPT = "first.md";

    private final Path rolesDirectory;
    private final ObjectMapper mapper = new ObjectMapper();

    public RoleCatalog(Path rolesDirectory) {
        this.rolesDirectory = rolesDirectory;
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * 列出所有合法角色名。非法名目录直接忽略并告警。
     *
     * @return 合法角色名列表（可能为空）
     */
    public List<String> list() {
        if (!Files.isDirectory(rolesDirectory)) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        try (Stream<Path> stream = Files.list(rolesDirectory)) {
            stream.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .forEach(name -> {
                        if (isValidName(name)) {
                            names.add(name);
                        } else {
                            log.warn("忽略非法角色目录: {}", name);
                        }
                    });
        } catch (Exception e) {
            log.warn("角色目录枚举失败: {} ({})", rolesDirectory, e.getMessage());
        }
        return names;
    }

    /**
     * 加载角色配置。
     *
     * @param name 角色名
     * @return 角色不存在或名字非法时为空
     */
    public Optional<RoleConfig> find(String name) {
        if (!isValidName(name)) {
            return Optional.empty();
        }
        Path directory = rolesDirectory.resolve(name);
        if (!Files.isDirectory(directory)) {
            return Optional.empty();
        }
        RoleFile roleFile = readRoleFile(directory);
        return Optional.of(new RoleConfig(
                name,
                roleFile.model(),
                roleFile.tools(),
                readPrompt(directory.resolve(SYSTEM_PROMPT)),
                readPrompt(directory.resolve(FIRST_PROMPT)),
                directory));
    }

    private RoleFile readRoleFile(Path directory) {
        Path file = directory.resolve(ROLE_FILE);
        if (!Files.isRegularFile(file)) {
            log.warn("角色缺少 role.json，按空配置继续: {}", directory.getFileName());
            return RoleFile.EMPTY;
        }
        try {
            RoleFile parsed = mapper.readValue(Files.readString(file), RoleFile.class);
            return parsed == null ? RoleFile.EMPTY : parsed;
        } catch (Exception e) {
            log.warn("角色配置非法，按空配置继续: {} ({})", file, e.getMessage());
            return RoleFile.EMPTY;
        }
    }

    private String readPrompt(Path file) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return Files.readString(file);
        } catch (Exception e) {
            log.warn("提示词读取失败，按缺失处理: {} ({})", file, e.getMessage());
            return null;
        }
    }

    /** {@code role.json} 的磁盘形态。 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RoleFile(String model, List<String> tools) {

        static final RoleFile EMPTY = new RoleFile(null, List.of());

        RoleFile {
            tools = tools == null ? List.of() : List.copyOf(tools);
        }
    }
}
