package cn.procsl.ping.boot.ai.core;

import cn.procsl.ping.boot.ai.session.MessageRecord;
import cn.procsl.ping.boot.ai.session.RoleSessionStore;
import cn.procsl.ping.boot.ai.session.SessionFile;
import cn.procsl.ping.boot.ai.support.StubChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SessionImageAndFlushTest {

    @TempDir
    Path temp;

    @Test
    void 图片被落盘并作为多模态输入发送() throws Exception {
        Path roleDir = Files.createDirectories(temp.resolve("roles/dev"));
        Path image = temp.resolve("a.png");
        Files.write(image, new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        StubChatModel model = new StubChatModel("ok");
        Session session = new Session("S1", "dev", "gpt", ChatClient.create(model), "sys", null,
                new RoleSessionStore(roleDir), new ImageStore(temp.resolve("images")), 40, null);

        session.submit(new Session.UserMessage("看图", List.of(image.toString())))
                .await(Duration.ofSeconds(5));

        Message last = model.prompts().getLast().getInstructions().getLast();
        assertThat(last).isInstanceOf(UserMessage.class);
        assertThat(((UserMessage) last).getMedia()).hasSize(1);
        assertThat(Files.list(temp.resolve("images"))).hasSize(1);
    }

    @Test
    void 流式累计五百字符后立即落盘() throws Exception {
        Path roleDir = Files.createDirectories(temp.resolve("roles/dev"));
        RoleSessionStore store = new RoleSessionStore(roleDir);
        FlushChatModel model = new FlushChatModel("x".repeat(600));
        Session session = new Session("S1", "dev", "gpt", ChatClient.create(model), "sys", null,
                store, null, 40, null);

        Completion completion = session.submit(new Session.UserMessage("hi"));

        String partial = awaitAssistantText(store, Duration.ofSeconds(5));
        assertThat(partial).hasSizeGreaterThanOrEqualTo(500);

        model.release.countDown();
        assertThat(completion.await(Duration.ofSeconds(5))).isTrue();
        assertThat(session.awaitIdle(Duration.ofSeconds(5))).isTrue();
    }

    private String awaitAssistantText(RoleSessionStore store, Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            SessionFile file = store.load("S1").orElse(null);
            if (file != null) {
                String text = file.messages().stream()
                        .filter(message -> MessageRecord.ASSISTANT.equals(message.type()))
                        .map(MessageRecord::text)
                        .filter(value -> value != null && !value.isEmpty())
                        .findFirst().orElse("");
                if (!text.isEmpty()) {
                    return text;
                }
            }
            Thread.sleep(50);
        }
        return "";
    }

    /** 先产出一大段再永久阻塞，用于验证流式落盘阈值。 */
    static final class FlushChatModel implements ChatModel {

        private final String chunk;
        private final java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);

        FlushChatModel(String chunk) {
            this.chunk = chunk;
        }

        @Override
        public ChatResponse call(Prompt prompt) {
            return new ChatResponse(List.of(new Generation(new AssistantMessage(chunk))));
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            reactor.core.publisher.Sinks.Many<ChatResponse> sink =
                    reactor.core.publisher.Sinks.many().unicast().onBackpressureBuffer();
            Thread.ofVirtual().start(() -> {
                try {
                    release.await(10, java.util.concurrent.TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                sink.tryEmitComplete();
            });
            sink.tryEmitNext(new ChatResponse(List.of(new Generation(new AssistantMessage(chunk)))));
            return sink.asFlux();
        }
    }
}
