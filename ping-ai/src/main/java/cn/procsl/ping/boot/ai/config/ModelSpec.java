package cn.procsl.ping.boot.ai.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 模型别名（{@code config.json.models.<alias>}）。
 *
 * @param provider    引用 {@code providers} 的键
 * @param model       传给 provider 的模型 ID
 * @param maxTokens   单次请求最大 token
 * @param temperature 采样温度
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ModelSpec(
        String provider,
        String model,
        @JsonProperty("max_tokens") Integer maxTokens,
        Double temperature) {
}
