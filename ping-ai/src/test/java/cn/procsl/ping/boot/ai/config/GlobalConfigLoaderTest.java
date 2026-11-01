package cn.procsl.ping.boot.ai.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalConfigLoaderTest {

    @TempDir
    Path temp;

    private final GlobalConfigLoader loader = new GlobalConfigLoader();

    @Test
    void 正常解析示例配置() throws Exception {
        Path file = temp.resolve("config.json");
        Files.writeString(file, """
                {
                  "version": 1,
                  "providers": {
                    "openai": { "api_key": "sk-1", "base_url": "https://api.example.com/v1", "timeout_seconds": 30 }
                  },
                  "models": {
                    "gpt5": { "provider": "openai", "model": "gpt-5", "max_tokens": 4096, "temperature": 0.2 }
                  },
                  "default_model": "gpt5",
                  "chat": { "max_history": 12 },
                  "unknown_field": true
                }
                """);

        GlobalConfig config = loader.load(file);

        assertThat(config.version()).isEqualTo(1);
        assertThat(config.providers()).containsKey("openai");
        assertThat(config.providers().get("openai").timeoutSecondsOrDefault()).isEqualTo(30);
        assertThat(config.models().get("gpt5").maxTokens()).isEqualTo(4096);
        assertThat(config.effectiveDefaultModel()).isEqualTo("gpt5");
        assertThat(config.chat().maxHistory()).isEqualTo(12);
    }

    @Test
    void 文件缺失时使用内置默认值() {
        GlobalConfig config = loader.load(temp.resolve("missing.json"));

        assertThat(config.version()).isEqualTo(GlobalConfig.CURRENT_VERSION);
        assertThat(config.models()).isEmpty();
        assertThat(config.chat().maxHistory()).isEqualTo(ChatSettings.DEFAULT_MAX_HISTORY);
    }

    @Test
    void JSON非法时降级不抛异常() throws Exception {
        Path file = temp.resolve("config.json");
        Files.writeString(file, "{ this is not json");

        GlobalConfig config = loader.load(file);

        assertThat(config.version()).isEqualTo(GlobalConfig.CURRENT_VERSION);
        assertThat(config.providers()).isEmpty();
    }

    @Test
    void 字段缺失时补默认值() throws Exception {
        Path file = temp.resolve("config.json");
        Files.writeString(file, """
                { "models": { "only": { "provider": "p", "model": "m" } } }
                """);

        GlobalConfig config = loader.load(file);

        assertThat(config.version()).isEqualTo(GlobalConfig.CURRENT_VERSION);
        assertThat(config.chat().maxHistory()).isEqualTo(ChatSettings.DEFAULT_MAX_HISTORY);
        assertThat(config.effectiveDefaultModel()).isEqualTo("only");
    }
}
