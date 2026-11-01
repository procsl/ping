package cn.procsl.ping.boot.ai.upstream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 请求重放。
 *
 * @param type 固定为 {@code pull}
 * @param from 起始对象 id，省略表示从头
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PullCommand(String type, Integer from) implements UpstreamMessage {

    public PullCommand {
        type = "pull";
    }

    /** 起始 id，省略时为 0。 */
    public int fromOrDefault() {
        return from == null || from < 0 ? 0 : from;
    }
}
