package cn.procsl.ping.boot.ai.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * 助手消息里的一次工具调用。
 *
 * @param id        调用 id
 * @param name      工具名
 * @param arguments 调用参数
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ToolCallRecord(
        String id,
        String name,
        @JsonProperty("arguments") Map<String, Object> arguments) {

    public ToolCallRecord {
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }
}
