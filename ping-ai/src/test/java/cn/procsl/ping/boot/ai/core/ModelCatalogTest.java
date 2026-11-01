package cn.procsl.ping.boot.ai.core;

import cn.procsl.ping.boot.ai.config.ChatSettings;
import cn.procsl.ping.boot.ai.config.GlobalConfig;
import cn.procsl.ping.boot.ai.config.ModelSpec;
import cn.procsl.ping.boot.ai.config.ProviderConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelCatalogTest {

    private static GlobalConfig config() {
        return new GlobalConfig(1,
                Map.of("openai", new ProviderConfig("sk", "https://api.example.com/v1", Map.of(), 30)),
                Map.of(
                        "gpt5", new ModelSpec("openai", "gpt-5", 4096, 0.2),
                        "mini", new ModelSpec("openai", "gpt-5-mini", null, null)),
                "gpt5",
                ChatSettings.defaults());
    }

    @Test
    void 命中别名() {
        ResolvedModel model = new ModelCatalog(config()).resolve("mini");

        assertThat(model.alias()).isEqualTo("mini");
        assertThat(model.providerName()).isEqualTo("openai");
        assertThat(model.spec().model()).isEqualTo("gpt-5-mini");
    }

    @Test
    void 未指定时使用默认模型() {
        assertThat(new ModelCatalog(config()).resolve(null).alias()).isEqualTo("gpt5");
    }

    @Test
    void 未命中时回落默认模型() {
        assertThat(new ModelCatalog(config()).resolve("missing").alias()).isEqualTo("gpt5");
    }

    @Test
    void 没有模型时抛出明确异常() {
        GlobalConfig empty = GlobalConfig.defaults();

        assertThatThrownBy(() -> new ModelCatalog(empty).resolve(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("未配置任何模型");
    }

    @Test
    void provider_未定义时抛出明确异常() {
        GlobalConfig broken = new GlobalConfig(1, Map.of(),
                Map.of("x", new ModelSpec("nope", "m", null, null)), "x", ChatSettings.defaults());

        assertThatThrownBy(() -> new ModelCatalog(broken).resolve("x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("provider");
    }
}
