package cn.procsl.ping.boot.ai.core;

import cn.procsl.ping.boot.ai.session.MessageRecord;
import cn.procsl.ping.boot.ai.session.ToolCallRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectProjectorTest {

    private final ObjectProjector projector = new ObjectProjector();

    @Test
    void 按消息映射对象() {
        List<MessageRecord> messages = List.of(
                MessageRecord.system("sys"),
                MessageRecord.user("问题"),
                new MessageRecord(MessageRecord.ASSISTANT, "回答", List.of(), null, false, null, null, null),
                new MessageRecord(MessageRecord.ASSISTANT, "", List.of(
                        new ToolCallRecord("c1", "read_file", Map.of("path", "a")),
                        new ToolCallRecord("c2", "grep", Map.of("q", "x"))), null, false, null, null, null),
                MessageRecord.tool("c1", "结果"),
                MessageRecord.error("internal", "fatal"));

        List<ProjectedObject> objects = projector.project(messages);

        assertThat(objects).extracting(ProjectedObject::id).containsExactly(0, 1, 2, 3, 4);
        assertThat(objects).extracting(ProjectedObject::type)
                .containsExactly(ObjectProjector.TYPE_USER, ObjectProjector.TYPE_TEXT,
                        ObjectProjector.TYPE_TOOL, ObjectProjector.TYPE_TOOL, ObjectProjector.TYPE_ERROR);
    }

    @Test
    void 重建幂等() {
        List<MessageRecord> messages = List.of(
                MessageRecord.user("q"),
                MessageRecord.assistant("a"));

        assertThat(projector.project(messages)).isEqualTo(projector.project(messages));
    }

    @Test
    void 中断消息带提示() {
        List<ProjectedObject> objects = projector.project(List.of(
                new MessageRecord(MessageRecord.ASSISTANT, "半句", List.of(), null, true, null, null, null)));

        assertThat(objects.getFirst().content()).isEqualTo("半句\n[输出中断]");
    }

    @Test
    void 已处理的错误被标记() {
        List<ProjectedObject> objects = projector.project(List.of(
                new MessageRecord(MessageRecord.ERROR, null, List.of(), null, null, "busy", "retry", true)));

        assertThat(objects.getFirst().handled()).isTrue();
    }
}
