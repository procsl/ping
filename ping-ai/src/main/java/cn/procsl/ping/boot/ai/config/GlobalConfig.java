package cn.procsl.ping.boot.ai.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局配置（{@code ~/.lwl/config.json}）。
 *
 * <p>它是独立的事实源：不注册为 Spring {@code PropertySource}，由 {@code SessionFactory}
 * 显式持有并显式构造模型客户端。</p>
 *
 * @param version      配置格式版本
 * @param providers    provider 定义容器
 * @param models       模型别名容器
 * @param defaultModel 未指定模型时的回落
 * @param chat         对话设置
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GlobalConfig(
        Integer version,
        Map<String, ProviderConfig> providers,
        Map<String, ModelSpec> models,
        @JsonProperty("default_model") String defaultModel,
        ChatSettings chat) {

    /** 当前配置格式版本。 */
    public static final int CURRENT_VERSION = 1;

    public GlobalConfig {
        version = version == null || version <= 0 ? CURRENT_VERSION : version;
        providers = providers == null ? Map.of() : Map.copyOf(providers);
        models = models == null ? Map.of() : new LinkedHashMap<>(models);
        chat = chat == null ? ChatSettings.defaults() : chat;
    }

    /** 内置默认值：没有 provider、没有模型。 */
    public static GlobalConfig defaults() {
        return new GlobalConfig(CURRENT_VERSION, Map.of(), Map.of(), null, ChatSettings.defaults());
    }

    /**
     * 生效的默认模型：显式声明优先，否则取第一个模型别名。
     *
     * @return 模型别名，可能为 {@code null}（未配置任何模型）
     */
    public String effectiveDefaultModel() {
        if (defaultModel != null && !defaultModel.isBlank()) {
            return defaultModel;
        }
        return models.isEmpty() ? null : models.keySet().iterator().next();
    }
}
