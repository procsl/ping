package cn.procsl.ping.boot.ai.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * provider 连接参数（{@code config.json.providers.<name>}）。
 *
 * @param apiKey         凭据；纯字符串，不支持环境变量引用
 * @param baseUrl        连接地址
 * @param headers        透传请求头
 * @param timeoutSeconds 请求超时（秒）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProviderConfig(
        @JsonProperty("api_key") String apiKey,
        @JsonProperty("base_url") String baseUrl,
        Map<String, String> headers,
        @JsonProperty("timeout_seconds") Integer timeoutSeconds) {

    public ProviderConfig {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    public int timeoutSecondsOrDefault() {
        return timeoutSeconds == null || timeoutSeconds <= 0 ? 60 : timeoutSeconds;
    }
}
