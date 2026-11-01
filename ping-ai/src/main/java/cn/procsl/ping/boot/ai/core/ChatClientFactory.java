package cn.procsl.ping.boot.ai.core;

import com.openai.client.OpenAIClientAsyncImpl;
import com.openai.client.OpenAIClientImpl;
import com.openai.core.ClientOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.http.okhttp.SpringAiOpenAiHttpClient;

import java.time.Duration;

/**
 * 显式构造模型客户端，不依赖 {@code spring.ai.*} 自动配置。
 */
public final class ChatClientFactory {

    /**
     * 按解析后的模型创建 {@link ChatClient}。
     *
     * @param model 解析结果
     * @return 可用于对话的客户端
     */
    public ChatClient create(ResolvedModel model) {
        Duration timeout = Duration.ofSeconds(model.provider().timeoutSecondsOrDefault());
        ClientOptions options = ClientOptions.Companion.builder()
                .apiKey(model.provider().apiKey())
                .baseUrl(model.provider().baseUrl())
                .timeout(timeout)
                .httpClient(SpringAiOpenAiHttpClient.builder().timeout(timeout).build())
                .build();

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(model.spec().model())
                .maxTokens(model.spec().maxTokens())
                .temperature(model.spec().temperature())
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiClient(new OpenAIClientImpl(options))
                .openAiClientAsync(new OpenAIClientAsyncImpl(options))
                .options(chatOptions)
                .build();

        return ChatClient.create(chatModel);
    }
}
