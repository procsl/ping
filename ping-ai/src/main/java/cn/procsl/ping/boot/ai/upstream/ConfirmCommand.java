package cn.procsl.ping.boot.ai.upstream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 确认结果。
 *
 * @param type      固定为 {@code confirm}
 * @param requestId 确认请求标识
 * @param decision  决定：allow / deny
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfirmCommand(
        String type,
        @JsonProperty("request_id") String requestId,
        String decision) implements UpstreamMessage {

    public ConfirmCommand {
        type = "confirm";
    }
}
