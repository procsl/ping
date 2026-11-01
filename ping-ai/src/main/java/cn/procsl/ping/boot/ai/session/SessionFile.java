package cn.procsl.ping.boot.ai.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 会话文件（{@code roles/<name>/sessions/<session-id>.json}）。
 *
 * @param sessionId 会话标识
 * @param role      角色名
 * @param model     模型别名
 * @param messages  消息表
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SessionFile(
        @JsonProperty("session_id") String sessionId,
        String role,
        String model,
        List<MessageRecord> messages) {

    public SessionFile {
        messages = messages == null ? List.of() : List.copyOf(messages);
    }
}
