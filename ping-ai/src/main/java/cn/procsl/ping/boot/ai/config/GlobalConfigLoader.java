package cn.procsl.ping.boot.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@code config.json} 加载器：读文件 → JSON 转 {@link GlobalConfig} → 异常时降级告警。
 *
 * <p>不依赖 Spring：可在 {@code SpringApplication.run()} 之前执行。</p>
 */
public final class GlobalConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(GlobalConfigLoader.class);

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 加载全局配置。文件缺失、语法非法或字段缺失都不会抛出异常。
     *
     * @param configFile 配置文件路径
     * @return 可用配置；任何异常都回落到内置默认值
     */
    public GlobalConfig load(Path configFile) {
        if (configFile == null || !Files.isRegularFile(configFile)) {
            log.warn("全局配置缺失，使用内置默认值: {}", configFile);
            return GlobalConfig.defaults();
        }
        try {
            GlobalConfig config = mapper.readValue(Files.readString(configFile), GlobalConfig.class);
            warnIfIncomplete(config);
            return config;
        } catch (Exception e) {
            log.warn("全局配置非法，使用内置默认值: {} ({})", configFile, e.getMessage());
            return GlobalConfig.defaults();
        }
    }

    private void warnIfIncomplete(GlobalConfig config) {
        if (config.providers().isEmpty()) {
            log.warn("全局配置缺少 providers，模型调用将不可用");
        }
        if (config.models().isEmpty()) {
            log.warn("全局配置缺少 models，模型调用将不可用");
        }
        config.models().forEach((alias, spec) -> {
            if (spec == null || spec.provider() == null || !config.providers().containsKey(spec.provider())) {
                log.warn("模型别名引用了不存在的 provider: {} -> {}", alias, spec == null ? null : spec.provider());
            }
        });
    }
}
