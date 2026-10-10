package cn.procsl.ping.boot.ui.component;

import cn.procsl.ping.boot.ui.TestUiApplication;
import cn.procsl.ping.boot.ui.component.api.ResourceReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 引用合法性（spec：仅被标注的接口可被装配描述引用 / 接口引用被解析为完整接口描述）。
 *
 * <p>夹具要点：{@code PlainFixture} <b>没有</b> {@link ResourceReference}，
 * 但它的 OpenAPI operation 带同名 summary —— 用于证明「只看注解、不按 summary 反查」。
 */
@SpringBootTest(
    classes = {TestUiApplication.class, AnnotatedFixture.class, PlainFixture.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UIComponentReferenceTest {

    private static final String ANNOTATED = "获取列表";
    private static final String PLAIN_ONLY = "仅未标注接口";

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Autowired
    UIComponentRegistry registry;

    private JsonNode json(String source) {
        try {
            return mapper.readTree(source);
        } catch (Exception e) {
            throw new IllegalStateException("测试夹具 JSON 无法解析", e);
        }
    }

    private String ref(String name) {
        return UIComponentRegistry.REFERENCE_PREFIX + name;
    }

    private JsonNode indexWith(String reference) {
        return json("""
            {
              "types": [],
              "pages": [ {"key": "p", "tree": {"type": "dataset", "id": "d", "api": "%s"}} ]
            }
            """.formatted(reference));
    }

    private JsonNode openApiFixture() {
        return json("""
            {
              "paths": {
                "/v1/ui-fixture/annotated": {
                  "get": {
                    "summary": "%s",
                    "parameters": [
                      {"name": "limit", "in": "query", "required": false, "schema": {"type": "integer"}}
                    ],
                    "responses": {
                      "200": {
                        "description": "ok",
                        "content": {
                          "application/json": {"schema": {"type": "object", "properties": {"id": {"type": "integer"}}}}
                        }
                      }
                    }
                  }
                },
                "/v1/ui-fixture/plain": {
                  "get": {
                    "summary": "%s",
                    "responses": { "200": { "description": "ok" } }
                  }
                }
              }
            }
            """.formatted(ANNOTATED, PLAIN_ONLY));
    }

    /** 只有带 @ResourceReference 的方法进索引；未标注方法即使在 OpenAPI 里也不进 */
    @Test
    void onlyAnnotatedMethodsEnterTheEndpointIndex() {
        Map<String, ApiEndpoint> index = registry.annotatedEndpoints();

        ApiEndpoint endpoint = index.get(ANNOTATED);
        assertNotNull(endpoint, "被标注的接口应进入索引");
        assertEquals("GET", endpoint.method());
        assertEquals("/v1/ui-fixture/annotated", endpoint.path());

        assertFalse(index.containsKey(PLAIN_ONLY),
            "未标注的方法不得进入索引，即使它的 OpenAPI summary 与引用同名");
        assertTrue(index.values().stream().noneMatch(e -> e.path().endsWith("/plain")),
            "索引中不得出现未标注接口的路径");
    }

    /** 被标注且在 OpenAPI 中存在 → 引用被替换为完整描述 */
    @Test
    void annotatedReferenceResolvesToFullDescription() {
        UIComponentRegistry target = new UIComponentRegistry(null);
        Map<String, ApiEndpoint> endpoints = Map.of(
            ANNOTATED, new ApiEndpoint("GET", "/v1/ui-fixture/annotated"));

        assertDoesNotThrow(() ->
            target.assemble(indexWith(ref(ANNOTATED)),
                openApiFixture(), endpoints));

        JsonNode api = target.pages().get(0).path("api");
        assertTrue(api.isObject(), "解析成功后 api 应为对象");
        assertEquals("GET", api.path("method").asString());
        assertEquals("/v1/ui-fixture/annotated", api.path("path").asString());
        assertEquals("integer",
            api.path("request").path("schema").path("properties").path("limit").path("type").asString(),
            "入参结构应来自 OpenAPI 的 parameters");
        assertEquals("200", api.path("response").path("status").asString());
        assertEquals("application/json", api.path("response").path("content_type").asString());
        assertEquals("integer",
            api.path("response").path("schema").path("properties").path("id").path("type").asString(),
            "出参结构应成为渲染数据契约");
    }

    /** 未标注的接口不可被引用：即使 OpenAPI 中存在同名 summary，引用也保持原样 */
    @Test
    void unannotatedEndpointCannotBeReferenced() {
        UIComponentRegistry target = new UIComponentRegistry(null);
        Map<String, ApiEndpoint> endpoints = Map.of(
            ANNOTATED, new ApiEndpoint("GET", "/v1/ui-fixture/annotated"));

        assertDoesNotThrow(() ->
            target.assemble(indexWith(ref(PLAIN_ONLY)),
                openApiFixture(), endpoints));

        JsonNode api = target.pages().get(0).path("api");
        assertTrue(api.isString(), "未标注接口的引用必须保持原样");
        assertEquals(ref(PLAIN_ONLY), api.asString());
    }

    /** 无入参接口 → 输入参数定义为空 */
    @Test
    void endpointWithoutParametersHasEmptyInputSchema() {
        UIComponentRegistry target = new UIComponentRegistry(null);
        Map<String, ApiEndpoint> endpoints = Map.of(
            PLAIN_ONLY, new ApiEndpoint("GET", "/v1/ui-fixture/plain"));

        assertDoesNotThrow(() ->
            target.assemble(indexWith(ref(PLAIN_ONLY)),
                openApiFixture(), endpoints));

        JsonNode api = target.pages().get(0).path("api");
        assertTrue(api.isObject());
        JsonNode properties = api.path("request").path("schema").path("properties");
        assertTrue(properties.isObject(), "入参结构必须是 object schema");
        assertEquals(0, properties.properties().size(), "无入参接口的输入参数定义应为空");
    }
}
