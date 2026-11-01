package cn.procsl.ping.boot.ai.core;

import cn.procsl.ping.boot.ai.config.ModelSpec;
import cn.procsl.ping.boot.ai.config.ProviderConfig;

/**
 * 解析后的模型：别名 + provider 连接参数 + 模型参数。
 *
 * @param alias        模型别名
 * @param providerName provider 键
 * @param provider     连接参数
 * @param spec         模型参数
 */
public record ResolvedModel(String alias, String providerName, ProviderConfig provider, ModelSpec spec) {
}
