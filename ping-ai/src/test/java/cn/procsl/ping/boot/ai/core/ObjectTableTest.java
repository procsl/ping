package cn.procsl.ping.boot.ai.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectTableTest {

    @Test
    void 流式对象的_seq_从零递增并以结束收尾() {
        ObjectTable table = new ObjectTable();
        int id = table.start("text");

        assertThat(table.push(id, "a").seq()).isZero();
        assertThat(table.push(id, "b").seq()).isEqualTo(1);
        assertThat(table.finish(id).seq()).isEqualTo(Box.END_SEQ);
        assertThat(table.isOpen(id)).isFalse();
        assertThat(table.get(id).orElseThrow().content()).isEqualTo("ab");
    }

    @Test
    void 完整对象是两段() {
        ObjectTable table = new ObjectTable();

        List<Box> boxes = table.complete("user", "hi");

        assertThat(boxes).hasSize(2);
        assertThat(boxes.get(0).seq()).isZero();
        assertThat(boxes.get(0).content()).isEqualTo("hi");
        assertThat(boxes.get(1).seq()).isEqualTo(Box.END_SEQ);
    }

    @Test
    void 多对象交错后按_id_归组内容完整() {
        ObjectTable table = new ObjectTable();
        int first = table.start("text");
        int second = table.start("tool");

        table.push(first, "你");
        table.push(second, "{");
        table.push(first, "好");
        table.push(second, "}");
        table.finish(second);
        table.finish(first);

        assertThat(table.get(first).orElseThrow().content()).isEqualTo("你好");
        assertThat(table.get(second).orElseThrow().content()).isEqualTo("{}");
        assertThat(table.nextId()).isEqualTo(2);
    }

    @Test
    void id_只增不复用() {
        ObjectTable table = new ObjectTable();
        int first = table.start("text");
        table.finish(first);
        int second = table.start("text");

        assertThat(first).isZero();
        assertThat(second).isEqualTo(1);
        assertThat(table.nextId()).isEqualTo(2);
    }

    @Test
    void 历史重放每个对象两条且按_id_升序() {
        ObjectTable table = new ObjectTable();
        table.complete("user", "q");
        int text = table.start("text");
        table.push(text, "a");
        table.finish(text);

        List<Box> history = table.history(1);

        assertThat(history).extracting(Box::id).containsExactly(1, 1);
        assertThat(history.get(0).content()).isEqualTo("a");
        assertThat(history.get(1).isEnd()).isTrue();
    }

    @Test
    void 未结束对象可被枚举() {
        ObjectTable table = new ObjectTable();
        int id = table.start("text");

        assertThat(table.hasOpen()).isTrue();
        assertThat(table.openIds()).containsExactly(id);
    }

    @Test
    void 已结束对象不可追加() {
        ObjectTable table = new ObjectTable();
        int id = table.start("text");
        table.finish(id);

        assertThatThrownBy(() -> table.push(id, "x")).isInstanceOf(IllegalStateException.class);
    }
}
