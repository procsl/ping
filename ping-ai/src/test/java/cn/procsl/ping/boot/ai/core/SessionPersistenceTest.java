package cn.procsl.ping.boot.ai.core;

import cn.procsl.ping.boot.ai.session.MessageRecord;
import cn.procsl.ping.boot.ai.session.RoleSessionStore;
import cn.procsl.ping.boot.ai.session.SessionFile;
import cn.procsl.ping.boot.ai.support.StubChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SessionPersistenceTest {

    @TempDir
    Path temp;

    private Path roleDir;
    private RoleSessionStore store;

    @BeforeEach
    void setUp() throws Exception {
        roleDir = Files.createDirectories(temp.resolve("roles/dev"));
        store = new RoleSessionStore(roleDir);
    }

    private Session session(ChatModel model, int maxHistory, SessionFile history) {
        return new Session("S1", "dev", "gpt", ChatClient.create(model), "sys", null,
                store, null, maxHistory, history);
    }

    @Test
    void 一轮结束后落盘用户与助手消息() {
        Session session = session(new StubChatModel("你", "好"), 40, null);

        session.submit(new Session.UserMessage("hi")).await(Duration.ofSeconds(5));

        SessionFile file = store.load("S1").orElseThrow();
        assertThat(file.messages()).extracting(MessageRecord::type)
                .containsExactly(MessageRecord.USER, MessageRecord.ASSISTANT);
        assertThat(file.messages().get(1).text()).isEqualTo("你好");
        assertThat(file.messages().get(1).interrupted()).isFalse();
    }

    @Test
    void 同一_request_id_只产生一条用户消息() {
        Session session = session(new StubChatModel("ok"), 40, null);

        session.submit(new Session.UserMessage("hi", List.of(), "r1")).await(Duration.ofSeconds(5));
        int before = session.messages().size();

        List<Box> replay = new java.util.ArrayList<>();
        Completion second = session.submit(new Session.UserMessage("hi", List.of(), "r1"));
        second.subscribe(replay::add);
        second.await(Duration.ofSeconds(5));

        assertThat(session.messages()).hasSize(before);
        assertThat(replay).extracting(Box::type).containsOnly(Session.TYPE_USER);
        assertThat(replay.getFirst().id()).isZero();
    }

    @Test
    void 错误落盘并在下一轮被标记已处理() {
        AtomicInteger calls = new AtomicInteger();
        ChatModel flaky = new StubChatModel(List.of("ok"), () -> {
            if (calls.getAndIncrement() == 0) {
                throw new IllegalStateException("boom");
            }
        });
        Session session = session(flaky, 40, null);

        session.submit(new Session.UserMessage("first")).await(Duration.ofSeconds(5));
        assertThat(store.load("S1").orElseThrow().messages())
                .anyMatch(message -> MessageRecord.ERROR.equals(message.type()));

        session.submit(new Session.UserMessage("second")).await(Duration.ofSeconds(5));

        SessionFile file = store.load("S1").orElseThrow();
        assertThat(file.messages())
                .anyMatch(message -> MessageRecord.ERROR.equals(message.type())
                        && Boolean.TRUE.equals(message.handled()));
        assertThat(session.history(0))
                .anyMatch(box -> Session.TYPE_ERROR.equals(box.type()) && box.content().contains("\"handled\":true"));
    }

    @Test
    void 取消把未完成的助手消息标记为中断() throws Exception {
        BlockingChatModel model = new BlockingChatModel();
        Session session = session(model, 40, null);

        Completion completion = session.submit(new Session.UserMessage("hi"));
        assertThat(model.started.await(5, TimeUnit.SECONDS)).isTrue();

        completion.cancel();
        assertThat(completion.await(Duration.ofSeconds(5))).isTrue();

        SessionFile file = store.load("S1").orElseThrow();
        assertThat(file.messages()).anyMatch(message -> MessageRecord.ASSISTANT.equals(message.type())
                && Boolean.TRUE.equals(message.interrupted()));
    }

    @Test
    void 恢复会话后对象表被重建() {
        SessionFile history = new SessionFile("S1", "dev", "gpt", List.of(
                MessageRecord.user("旧问题"),
                new MessageRecord(MessageRecord.ASSISTANT, "旧回答", List.of(), null, false, null, null, null),
                new MessageRecord(MessageRecord.ERROR, null, List.of(), null, null, "busy", "retry", true)));
        Session session = session(new StubChatModel("x"), 40, history);

        assertThat(session.objects()).extracting(ObjectTable.Entry::type)
                .containsExactly(ObjectProjector.TYPE_USER, ObjectProjector.TYPE_TEXT, ObjectProjector.TYPE_ERROR);
        assertThat(session.objects().get(2).handled()).isTrue();
        assertThat(session.history(2).getFirst().content()).contains("\"handled\":true");
    }

    @Test
    void 历史窗口只保留最近消息() {
        SessionFile history = new SessionFile("S1", "dev", "gpt", List.of(
                MessageRecord.user("u1"),
                new MessageRecord(MessageRecord.ASSISTANT, "a1", List.of(), null, false, null, null, null),
                MessageRecord.user("u2"),
                new MessageRecord(MessageRecord.ASSISTANT, "a2", List.of(), null, false, null, null, null)));
        StubChatModel model = new StubChatModel("ok");
        Session session = session(model, 2, history);

        session.submit(new Session.UserMessage("new")).await(Duration.ofSeconds(5));

        List<org.springframework.ai.chat.messages.Message> sent = model.prompts().getLast().getInstructions();
        assertThat(sent).extracting(org.springframework.ai.chat.messages.Message::getText)
                .doesNotContain("u1", "a1");
        assertThat(sent.getLast().getText()).isEqualTo("new");
    }

    /** 先产出一段再永久阻塞，用于验证取消。 */
    static final class BlockingChatModel implements ChatModel {

        private final CountDownLatch started = new CountDownLatch(1);

        @Override
        public ChatResponse call(Prompt prompt) {
            return new ChatResponse(List.of(new Generation(new AssistantMessage("x"))));
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            started.countDown();
            return Flux.concat(
                    Flux.just(new ChatResponse(List.of(new Generation(new AssistantMessage("半"))))),
                    Flux.never());
        }
    }
}
