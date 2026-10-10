package cn.procsl.ping.boot.ui.component;

import cn.procsl.ping.boot.ui.TestUiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 降级策略验证：索引缺失 / 装配条目损坏 / OpenAPI 缺失 / 引用未命中，
 * 四例都必须不抛异常、上下文可正常启动（spec：启动永不失败）。
 */
@SpringBootTest(
    classes = TestUiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UIComponentRegistryFallbackTest {

    private static final String RAW_REFERENCE = "@ResourceReference:获取用户列表";

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Autowired
    UIComponentRegistry sharedRegistry;

    private UIComponentRegistry newRegistry() {
        // 直接构造，避免污染共享上下文中的注册表快照
        return new UIComponentRegistry(null);
    }

    private JsonNode json(String source) {
        try {
            return mapper.readTree(source);
        } catch (Exception e) {
            throw new IllegalStateException("测试夹具 JSON 无法解析", e);
        }
    }

    @Test
    void contextStartsNormally() {
        assertNotNull(sharedRegistry, "注册表 bean 必须存在");
        assertNotNull(sharedRegistry.snapshot(), "注册表快照必须可取，上下文需正常启动");
    }

    /** 索引缺失 → 注册表空，且不抛异常 */
    @Test
    void missingIndexYieldsEmptyRegistry() {
        UIComponentRegistry registry = newRegistry();
        assertDoesNotThrow(() -> registry.assemble(null, null, Map.of()));
        assertTrue(registry.types().isEmpty(), "索引缺失时类型定义应为空");
        assertTrue(registry.pages().isEmpty(), "索引缺失时页面应为空");
    }

    /** 装配条目损坏 → 跳过该页并告警，其余仍正常装配 */
    @Test
    void malformedEntryIsSkippedAndOthersStillLoad() {
        JsonNode index = json("""
            {
              "schema_version": 1,
              "types": [ {"type": "dataset", "name": "数据表"} ],
              "pages": [
                {"key": "bad", "tree": "不是对象"},
                {"key": "also-bad"},
                {"key": "good", "tree": {"type": "application", "id": "ping.ui.app"}}
              ]
            }
            """);

        UIComponentRegistry registry = newRegistry();
        assertDoesNotThrow(() -> registry.assemble(index, null, Map.of()));
        assertEquals(1, registry.pages().size(), "损坏条目应被跳过，合法条目必须保留");
        assertEquals(1, registry.types().size());
    }

    /** OpenAPI 文档缺失 → 引用保持原样 */
    @Test
    void missingOpenApiKeepsReferenceRaw() {
        JsonNode index = json("""
            {
              "types": [],
              "pages": [ {"key": "p", "tree": {"type": "menu", "id": "m", "api": "%s"}} ]
            }
            """.formatted(RAW_REFERENCE));
        Map<String, ApiEndpoint> endpoints = Map.of(
            "获取用户列表", new ApiEndpoint("GET", "/v1/system/users"));

        UIComponentRegistry registry = newRegistry();
        assertDoesNotThrow(() -> registry.assemble(index, null, endpoints));

        JsonNode api = registry.pages().get(0).path("api");
        assertTrue(api.isString(), "OpenAPI 缺失时 api 应保持字符串引用");
        assertEquals(RAW_REFERENCE, api.asString());
    }

    /** 引用目标不存在（未标注的接口不会进索引）→ 引用保持原样 */
    @Test
    void unknownReferenceKeepsRawAndDoesNotThrow() {
        JsonNode index = json("""
            {
              "types": [],
              "pages": [ {"key": "p", "tree": {"type": "menu", "id": "m", "api": "%s"}} ]
            }
            """.formatted(RAW_REFERENCE));
        JsonNode openapi = json("""
            {
              "paths": {
                "/v1/system/users": {
                  "get": {"summary": "用户列表", "responses": {"200": {"description": "ok"}}}
                }
              }
            }
            """);

        UIComponentRegistry registry = newRegistry();
        assertDoesNotThrow(() -> registry.assemble(index, openapi, Map.of()));

        JsonNode api = registry.pages().get(0).path("api");
        assertTrue(api.isString(), "引用未命中时 api 应保持原样");
        assertEquals(RAW_REFERENCE, api.asString());
    }

    /** 索引不是 JSON 对象 → 空注册表而非异常 */
    @Test
    void nonObjectIndexYieldsEmptyRegistry() {
        UIComponentRegistry registry = newRegistry();
        assertDoesNotThrow(() -> registry.assemble(json("\"我是一个字符串\""), null, Map.of()));
        assertTrue(registry.types().isEmpty());
        assertTrue(registry.pages().isEmpty());
    }
}
