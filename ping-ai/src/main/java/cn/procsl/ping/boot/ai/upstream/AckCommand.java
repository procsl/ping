package cn.procsl.ping.boot.ai.upstream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 投递进度确认。
 *
 * @param type 固定为 {@code ack}
 * @param id   已收到的对象 id
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AckCommand(String type, Integer id) implements UpstreamMessage {

    public AckCommand {
        type = "ack";
    }
}
