package cn.procsl.ping.boot.ai.core;

import cn.procsl.ping.boot.ai.config.GlobalConfig;
import cn.procsl.ping.boot.ai.config.ModelSpec;
import cn.procsl.ping.boot.ai.config.ProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 按名解析模型到连接参数。
 *
 * <p>未命中时回落到全局默认模型并告警；完全没有可用模型时抛出明确异常，
 * 由适配器转成「配置不可读」退出码。</p>
 */
public final class ModelCatalog {

    private static final Logger log = LoggerFactory.getLogger(ModelCatalog.class);

    private final GlobalConfig config;

    public ModelCatalog(GlobalConfig config) {
        this.config = config;
    }

    /**
     * 解析模型。
     *
     * @param alias 模型别名；为空表示使用全局默认
     * @return 解析结果
     * @throws IllegalStateException 没有任何可用模型或 provider 未定义
     */
    public ResolvedModel resolve(String alias) {
        String requested = alias == null || alias.isBlank() ? config.effectiveDefaultModel() : alias.trim();
        if (requested == null) {
            throw new IllegalStateException("未配置任何模型");
        }
        ModelSpec spec = config.models().get(requested);
        if (spec == null) {
            String fallback = config.effectiveDefaultModel();
            log.warn("模型别名不存在，回落到默认模型: {} -> {}", requested, fallback);
            requested = fallback;
            spec = requested == null ? null : config.models().get(requested);
        }
        if (spec == null) {
            throw new IllegalStateException("未配置任何模型");
        }
        ProviderConfig provider = config.providers().get(spec.provider());
        if (provider == null) {
            throw new IllegalStateException("模型引用了不存在的 provider: " + spec.provider());
        }
        return new ResolvedModel(requested, spec.provider(), provider, spec);
    }
}
