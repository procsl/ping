package cn.procsl.ping.boot.ai.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CompletionTest {

    @Test
    void 订阅者持续收到后续_box() {
        Completion completion = new Completion(null);
        List<Box> boxes = new ArrayList<>();
        completion.subscribe(boxes::add);

        completion.deliver(new Box("text", 0, 0, "a"));
        completion.deliver(new Box("text", 0, Box.END_SEQ, ""));

        assertThat(boxes).hasSize(2);
    }

    @Test
    void 晚订阅会补发已产出的_box() {
        Completion completion = new Completion(null);
        completion.deliver(new Box("text", 0, 0, "a"));

        List<Box> boxes = new ArrayList<>();
        completion.subscribe(boxes::add);
        completion.deliver(new Box("text", 0, 1, "b"));

        assertThat(boxes).extracting(Box::content).containsExactly("a", "b");
    }

    @Test
    void 等待在结束时返回() {
        Completion completion = new Completion(null);
        assertThat(completion.await(Duration.ofMillis(10))).isFalse();

        completion.finish();

        assertThat(completion.await(Duration.ofSeconds(1))).isTrue();
        assertThat(completion.isFinished()).isTrue();
    }

    @Test
    void 取消只执行一次() {
        AtomicInteger calls = new AtomicInteger();
        Completion completion = new Completion(calls::incrementAndGet);

        completion.cancel();
        completion.cancel();

        assertThat(calls.get()).isEqualTo(1);
        assertThat(completion.cancelled()).isTrue();
    }
}
