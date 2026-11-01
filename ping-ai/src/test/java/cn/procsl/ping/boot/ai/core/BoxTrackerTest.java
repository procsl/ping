package cn.procsl.ping.boot.ai.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoxTrackerTest {

    private final List<Integer> gaps = new ArrayList<>();
    private final List<Integer> acks = new ArrayList<>();
    private final BoxTracker tracker = new BoxTracker(gaps::add, acks::add);

    @Test
    void 按_id_归组并拼接片段() {
        tracker.onBox(new Box("text", 0, 0, "你"));
        tracker.onBox(new Box("text", 0, 1, "好"));
        tracker.onBox(new Box("text", 0, Box.END_SEQ, ""));

        assertThat(tracker.objects()).hasSize(1);
        assertThat(tracker.objects().getFirst().content()).isEqualTo("你好");
        assertThat(tracker.objects().getFirst().open()).isFalse();
        assertThat(acks).containsExactly(0, 0, 0);
    }

    @Test
    void 对象_id_缺口触发重放() {
        tracker.onBox(new Box("text", 1, 0, "a"));

        assertThat(gaps).containsExactly(0);
        assertThat(tracker.objects()).isEmpty();
    }

    @Test
    void 片段序号缺口丢弃对象并触发重放() {
        tracker.onBox(new Box("text", 0, 0, "a"));
        tracker.onBox(new Box("text", 0, 2, "c"));

        assertThat(gaps).containsExactly(0);
        assertThat(tracker.objects()).isEmpty();
    }

    @Test
    void 断线重置后从头重建() {
        tracker.onBox(new Box("text", 0, 0, "a"));
        tracker.reset();

        assertThat(tracker.objects()).isEmpty();
        assertThat(tracker.expected()).isZero();

        tracker.onBox(new Box("text", 0, 0, "a"));
        assertThat(tracker.objects()).hasSize(1);
    }

    @Test
    void 重复片段被忽略() {
        tracker.onBox(new Box("text", 0, 0, "a"));
        tracker.onBox(new Box("text", 0, Box.END_SEQ, ""));
        tracker.onBox(new Box("text", 0, Box.END_SEQ, ""));

        assertThat(tracker.objects()).hasSize(1);
        assertThat(tracker.objects().getFirst().content()).isEqualTo("a");
    }
}
