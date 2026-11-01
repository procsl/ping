package cn.procsl.ping.boot.ai.core;

import cn.procsl.ping.boot.ai.support.StubChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class SessionTest {

    private static Session session(StubChatModel model) {
        return new Session("s1", ChatClient.create(model), "系统提示词", null);
    }

    @Test
    void 一轮对话先回送用户对象再流式输出文本() {
        Session session = session(new StubChatModel("你", "好"));
        List<Box> boxes = Collections.synchronizedList(new ArrayList<>());

        Completion completion = session.submit(new Session.UserMessage("hi"));
        completion.subscribe(boxes::add);

        assertThat(completion.await(Duration.ofSeconds(5))).isTrue();
        assertThat(boxes).extracting(Box::type, Box::id, Box::seq)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(Session.TYPE_USER, 0, 0),
                        org.assertj.core.groups.Tuple.tuple(Session.TYPE_USER, 0, Box.END_SEQ),
                        org.assertj.core.groups.Tuple.tuple(Session.TYPE_TEXT, 1, 0),
                        org.assertj.core.groups.Tuple.tuple(Session.TYPE_TEXT, 1, 1),
                        org.assertj.core.groups.Tuple.tuple(Session.TYPE_TEXT, 1, Box.END_SEQ));
        assertThat(session.state()).isEqualTo(Session.State.IDLE);
        assertThat(session.objects()).noneMatch(entry -> entry.open());
    }

    @Test
    void 上一轮未结束时拒绝新提交且不分配用户对象() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Session session = session(new StubChatModel(List.of("a"), () -> {
            started.countDown();
            await(release);
        }));

        Completion first = session.submit(new Session.UserMessage("first"));
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();

        List<Box> second = Collections.synchronizedList(new ArrayList<>());
        Completion rejected = session.submit(new Session.UserMessage("second"));
        rejected.subscribe(second::add);
        assertThat(rejected.await(Duration.ofSeconds(5))).isTrue();

        assertThat(second).extracting(Box::type).containsOnly(Session.TYPE_ERROR);
        assertThat(second.getFirst().content()).contains(Session.CODE_BUSY);

        release.countDown();
        assertThat(first.await(Duration.ofSeconds(5))).isTrue();
        assertThat(session.objects()).noneMatch(entry -> entry.type().equals(Session.TYPE_USER)
                && entry.content().contains("second"));
    }

    @Test
    void 取消后所有对象结束且回到空闲() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Session session = session(new StubChatModel(List.of("a"), started::countDown));

        Completion completion = session.submit(new Session.UserMessage("hi"));
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();

        completion.cancel();

        assertThat(completion.await(Duration.ofSeconds(5))).isTrue();
        assertThat(session.awaitIdle(Duration.ofSeconds(5))).isTrue();
        assertThat(session.objects()).noneMatch(entry -> entry.open());
    }

    @Test
    void 空消息返回输入错误且不启动轮次() {
        Session session = session(new StubChatModel("x"));
        List<Box> boxes = Collections.synchronizedList(new ArrayList<>());

        Completion completion = session.submit(new Session.UserMessage("  "));
        completion.subscribe(boxes::add);

        assertThat(completion.await(Duration.ofSeconds(1))).isTrue();
        assertThat(boxes).extracting(Box::type).containsOnly(Session.TYPE_ERROR);
        assertThat(boxes.getFirst().content()).contains(Session.CODE_INVALID_INPUT);
        assertThat(session.state()).isEqualTo(Session.State.IDLE);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
