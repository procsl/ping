package cn.procsl.ping.boot.ai.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 会话消息记录（落盘形态）。
 *
 * <p>只有消息表落盘，对象表由 {@code ObjectProjector} 重建。
 * 错误也是消息记录的一种，但不进入模型上下文。</p>
 *
 * @param type        消息类型：system / user / assistant / tool / error
 * @param text        文本内容
 * @param toolCalls   助手消息发起的工具调用
 * @param toolCallId  工具结果对应的调用 id
 * @param interrupted 流式消息是否被中断
 * @param code        错误码（仅 error）
 * @param status      错误处理方式（仅 error）
 * @param handled     错误是否已处理（仅 error）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageRecord(
        String type,
        String text,
        @JsonProperty("tool_calls") List<ToolCallRecord> toolCalls,
        @JsonProperty("tool_call_id") String toolCallId,
        Boolean interrupted,
        String code,
        String status,
        Boolean handled) {

    /** 系统消息。 */
    public static final String SYSTEM = "system";
    /** 用户消息。 */
    public static final String USER = "user";
    /** 助手消息。 */
    public static final String ASSISTANT = "assistant";
    /** 工具结果消息。 */
    public static final String TOOL = "tool";
    /** 错误消息。 */
    public static final String ERROR = "error";

    public MessageRecord {
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }

    public static MessageRecord system(String text) {
        return new MessageRecord(SYSTEM, text, List.of(), null, null, null, null, null);
    }

    public static MessageRecord user(String text) {
        return new MessageRecord(USER, text, List.of(), null, null, null, null, null);
    }

    public static MessageRecord assistant(String text) {
        return new MessageRecord(ASSISTANT, text, List.of(), null, false, null, null, null);
    }

    public static MessageRecord tool(String toolCallId, String text) {
        return new MessageRecord(TOOL, text, List.of(), toolCallId, null, null, null, null);
    }

    public static MessageRecord error(String code, String status) {
        return new MessageRecord(ERROR, null, List.of(), null, null, code, status, false);
    }

    public MessageRecord withText(String value) {
        return new MessageRecord(type, value, toolCalls, toolCallId, interrupted, code, status, handled);
    }

    public MessageRecord withInterrupted(boolean value) {
        return new MessageRecord(type, text, toolCalls, toolCallId, value, code, status, handled);
    }

    public MessageRecord withToolCalls(List<ToolCallRecord> value) {
        return new MessageRecord(type, text, value, toolCallId, interrupted, code, status, handled);
    }

    public MessageRecord withHandled(boolean value) {
        return new MessageRecord(type, text, toolCalls, toolCallId, interrupted, code, status, value);
    }
}
