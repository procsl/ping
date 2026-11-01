package cn.procsl.ping.boot.ai.support;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 测试替身：按片段产出固定文本，不访问网络。
 */
public class StubChatModel implements ChatModel {

    private final List<String> chunks;
    private final Runnable beforeStream;
    private final List<Prompt> prompts = new CopyOnWriteArrayList<>();

    public StubChatModel(String... chunks) {
        this(List.of(chunks), () -> { });
    }

    public StubChatModel(List<String> chunks, Runnable beforeStream) {
        this.chunks = List.copyOf(chunks);
        this.beforeStream = beforeStream;
    }

    /** 收到的请求，按时间顺序。 */
    public List<Prompt> prompts() {
        return List.copyOf(prompts);
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        prompts.add(prompt);
        return new ChatResponse(List.of(new Generation(new AssistantMessage(String.join("", chunks)))));
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        prompts.add(prompt);
        beforeStream.run();
        return Flux.fromIterable(chunks)
                .map(chunk -> new ChatResponse(List.of(new Generation(new AssistantMessage(chunk)))));
    }
}
