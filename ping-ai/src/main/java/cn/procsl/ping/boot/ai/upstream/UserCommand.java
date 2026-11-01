package cn.procsl.ping.boot.ai.upstream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 用户消息提交。
 *
 * @param type      固定为 {@code user}
 * @param text      文本，可为空
 * @param images    图片引用，可为空
 * @param requestId 可选请求标识，用于去重
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserCommand(
        String type,
        String text,
        List<String> images,
        @JsonProperty("request_id") String requestId) implements UpstreamMessage {

    public UserCommand {
        type = "user";
        images = images == null ? List.of() : List.copyOf(images);
    }
}
