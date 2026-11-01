package cn.procsl.ping.boot.ai.core;

import cn.procsl.ping.boot.ai.session.MessageRecord;
import cn.procsl.ping.boot.ai.session.ToolCallRecord;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息表 → 对象表的唯一投影实现。
 *
 * <p>实时推送与全量重建共用同一映射，保证重建幂等、流式与全量等效。</p>
 */
public final class ObjectProjector {

    /** 用户消息对象类型。 */
    public static final String TYPE_USER = "user";
    /** 助手文本对象类型。 */
    public static final String TYPE_TEXT = "text";
    /** 工具调用对象类型。 */
    public static final String TYPE_TOOL = "tool";
    /** 错误对象类型。 */
    public static final String TYPE_ERROR = "error";

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 全量重建。
     *
     * @param messages 消息表（按时间顺序）
     * @return 对象表（id 从 0 递增）
     */
    public List<ProjectedObject> project(List<MessageRecord> messages) {
        List<ProjectedObject> objects = new ArrayList<>();
        if (messages == null) {
            return objects;
        }
        int id = 0;
        for (MessageRecord message : messages) {
            switch (message.type()) {
                case MessageRecord.USER -> objects.add(new ProjectedObject(id++, TYPE_USER, nullToEmpty(message.text()), false));
                case MessageRecord.ASSISTANT -> id = projectAssistant(message, id, objects);
                case MessageRecord.ERROR -> objects.add(new ProjectedObject(id++, TYPE_ERROR,
                        errorContent(message), Boolean.TRUE.equals(message.handled())));
                default -> {
                    // system 与 tool 不单独成对象：tool 结果并入对应的 tool call 对象
                }
            }
        }
        return objects;
    }

    private int projectAssistant(MessageRecord message, int id, List<ProjectedObject> objects) {
        String text = nullToEmpty(message.text());
        if (!text.isBlank()) {
            String content = Boolean.TRUE.equals(message.interrupted()) ? text + "\n[输出中断]" : text;
            objects.add(new ProjectedObject(id++, TYPE_TEXT, content, false));
        }
        for (ToolCallRecord call : message.toolCalls()) {
            objects.add(new ProjectedObject(id++, TYPE_TOOL, toolContent(call), false));
        }
        return id;
    }

    private String toolContent(ToolCallRecord call) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("name", call.name());
        content.put("arguments", call.arguments());
        return mapper.writeValueAsString(content);
    }

    private String errorContent(MessageRecord message) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("code", message.code());
        content.put("status", message.status());
        return mapper.writeValueAsString(content);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
