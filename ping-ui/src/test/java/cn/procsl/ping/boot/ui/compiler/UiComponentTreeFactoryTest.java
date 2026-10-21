package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import cn.procsl.ping.boot.ui.exception.ComposeException;
import cn.procsl.ping.boot.ui.repository.ComposeRepository;
import cn.procsl.ping.boot.ui.repository.OpenApiRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 编译组装入口测试（任务 3.4 与边界契约）：
 * 缺 openapi 输入失败、无 compose 空树、正常输入端到端编译。
 */
class UiComponentTreeFactoryTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void failsWhenComposeExistsWithoutOpenApi() {
        ComposeRepository composeRepository = new ComposeRepository(mapper);
        OpenApiRepository emptyOpenApi = new OpenApiRepository(mapper) {
            @Override
            public List<JsonNode> loadDocuments() {
                return List.of();
            }
        };

        ComposeException e = assertThrows(ComposeException.class,
            () -> UiComponentTreeFactory.create(composeRepository, emptyOpenApi));

        assertThat(e.getMessage()).contains("compose.json");
        assertThat(e.getMessage()).contains("openapi-*.json");
    }

    @Test
    void emptyComposeInputYieldsEmptyAppTree() {
        ComposeRepository emptyCompose = new ComposeRepository(mapper) {
            @Override
            public List<JsonNode> loadAll() {
                return List.of();
            }
        };
        OpenApiRepository emptyOpenApi = new OpenApiRepository(mapper) {
            @Override
            public List<JsonNode> loadDocuments() {
                return List.of();
            }
        };

        Component app = UiComponentTreeFactory.create(emptyCompose, emptyOpenApi);

        assertThat(app.getType()).isEqualTo("app");
        assertThat(app.getChildren()).isEmpty();
    }

    @Test
    void normalInputCompilesFullTree() {
        Component app = UiComponentTreeFactory.create(
            new ComposeRepository(mapper), new OpenApiRepository(mapper));

        assertThat(app.getType()).isEqualTo("app");
        assertThat(app.getChildren()).hasSize(1);
        Component page = app.getChildren().getFirst();
        assertThat(page.getType()).isEqualTo("page");
        assertThat(page.getChildren()).hasSize(2);
        assertThat(page.getChildren().getFirst().getProperties().get("method").asString()).isEqualTo("GET");
    }

    @Test
    void defaultEntryMatchesInjectedEntry() {
        Component viaDefault = UiComponentTreeFactory.create();
        Component viaInjected = UiComponentTreeFactory.create(
            new ComposeRepository(mapper), new OpenApiRepository(mapper));

        // Component 无 equals：比较 JSON 序列化形态
        assertThat(mapper.writeValueAsString(viaDefault))
            .isEqualTo(mapper.writeValueAsString(viaInjected));
    }
}
