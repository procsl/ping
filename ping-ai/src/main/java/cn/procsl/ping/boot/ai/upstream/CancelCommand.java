package cn.procsl.ping.boot.ai.upstream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 取消当前轮次。
 *
 * @param type 固定为 {@code cancel}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CancelCommand(String type) implements UpstreamMessage {

    public CancelCommand {
        type = "cancel";
    }
}
