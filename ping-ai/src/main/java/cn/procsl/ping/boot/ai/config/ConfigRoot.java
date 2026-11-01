package cn.procsl.ping.boot.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 配置根（默认 {@code ~/.lwl}）。
 *
 * <p>负责解析路径并一次性创建 {@code logs/} {@code images/} {@code roles/} 骨架。
 * 目录不可写时降级告警，不抛异常——启动永不因配置目录失败而中断。</p>
 */
public final class ConfigRoot {

    private static final Logger log = LoggerFactory.getLogger(ConfigRoot.class);

    /** 默认目录名。 */
    public static final String DEFAULT_DIRECTORY = ".lwl";

    private final Path root;
    private final boolean writable;

    private ConfigRoot(Path root, boolean writable) {
        this.root = root;
        this.writable = writable;
    }

    /**
     * 解析配置根路径。
     *
     * @param override 命令行覆盖值，可为空
     * @return 配置根；尚未创建目录
     */
    public static ConfigRoot resolve(String override) {
        if (override != null && !override.isBlank()) {
            return new ConfigRoot(expandHome(override.trim()), false);
        }
        return new ConfigRoot(Path.of(System.getProperty("user.home"), DEFAULT_DIRECTORY), false);
    }

    private static Path expandHome(String raw) {
        if (raw.equals("~")) {
            return Path.of(System.getProperty("user.home"));
        }
        if (raw.startsWith("~/")) {
            return Path.of(System.getProperty("user.home"), raw.substring(2));
        }
        return Path.of(raw);
    }

    /**
     * 创建目录骨架。不可写时降级并返回 {@code false}。
     *
     * @return 目录是否可写
     */
    public ConfigRoot initialize() {
        boolean ok = true;
        for (Path dir : new Path[]{root, logs(), images(), roles()}) {
            try {
                Files.createDirectories(dir);
            } catch (IOException | SecurityException e) {
                log.warn("配置目录不可创建，降级继续: {} ({})", dir, e.getMessage());
                ok = false;
            }
        }
        if (ok && !Files.isWritable(root)) {
            log.warn("配置目录不可写，降级继续: {}", root);
            ok = false;
        }
        return new ConfigRoot(root, ok);
    }

    public Path root() {
        return root;
    }

    public Path logs() {
        return root.resolve("logs");
    }

    public Path images() {
        return root.resolve("images");
    }

    public Path roles() {
        return root.resolve("roles");
    }

    public Path configFile() {
        return root.resolve("config.json");
    }

    public boolean writable() {
        return writable;
    }
}
