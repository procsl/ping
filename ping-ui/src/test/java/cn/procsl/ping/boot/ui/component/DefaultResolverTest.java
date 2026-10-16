package cn.procsl.ping.boot.ui.component;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.*;

class DefaultResolverTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DefaultResolver resolver = new DefaultResolver();

    /** operation properties fixture（已 $ref 展开） */
    private JsonNode operation() {
        return objectMapper.readTree("""
                {
                  "method": "GET",
                  "path": "/v1/system/users",
                  "request": {
                    "query": {
                      "type": "object",
                      "properties": {
                        "name": { "type": "string" },
                        "state": { "type": "string", "enum": ["disable", "enable"] }
                      }
                    },
                    "offset": { "type": "integer", "default": 1 },
                    "size": { "type": "integer", "default": 10 },
                    "sort": { "type": "array", "items": { "type": "string" } }
                  },
                  "response": {
                    "content": {
                      "type": "array",
                      "items": { "type": "object", "properties": { "name": { "type": "string" } } }
                    },
                    "total": { "type": "integer" }
                  }
                }
                """);
    }

    private void withDefaults(JsonNode operation, String defaultsJson) {
        ((ObjectNode) operation).set("defaults", objectMapper.readTree(defaultsJson));
    }

    private JsonNode requestState(JsonNode operation) {
        return operation.get("request").get("query").get("properties").get("state");
    }

    // --- 1.5.1 operation defaults 写入字段 default ---

    @Test
    void shouldApplyOperationDefaultsByFieldName() {
        JsonNode operation = operation();
        withDefaults(operation, """
                { "request": { "state": "enable" } }
                """);

        resolver.applyOperationDefaults(operation);

        assertEquals("enable", requestState(operation).get("default").asString(),
                "字段名简写应定位到 query.state 并写入 default");
    }

    @Test
    void shouldApplyOperationDefaultsByPath() {
        JsonNode operation = operation();
        withDefaults(operation, """
                { "request": { "query.state": "enable" } }
                """);

        resolver.applyOperationDefaults(operation);

        assertEquals("enable", requestState(operation).get("default").asString());
    }

    @Test
    void shouldApplyOperationDefaultsToResponseAndArray() {
        JsonNode operation = operation();
        withDefaults(operation, """
                { "response": { "total": 0, "content[].name": "anonymous" } }
                """);

        resolver.applyOperationDefaults(operation);

        assertEquals(0, operation.get("response").get("total").get("default").asInt());
        JsonNode itemName = operation.get("response").get("content").get("items").get("properties").get("name");
        assertEquals("anonymous", itemName.get("default").asString());
    }

    // --- 1.5.2 function overrides 覆盖 operation defaults ---

    @Test
    void shouldApplyOverridesOverOperationDefaults() {
        JsonNode operation = operation();
        withDefaults(operation, """
                { "request": { "state": "enable" } }
                """);
        JsonNode function = objectMapper.readTree("""
                { "type": "function", "id": "system/user",
                  "properties": { "overrides": { "request": { "state": "disable" } } } }
                """);

        resolver.applyOperationDefaults(operation);
        resolver.applyOverrides(function.get("properties"), operation);

        assertEquals("disable", requestState(operation).get("default").asString(),
                "overrides 应覆盖 operation defaults");
    }

    // --- 1.5.3 优先级链：实际调用值 > overrides > operation defaults ---

    @Test
    void shouldPreferOverridesOverOperationDefaults() {
        JsonNode operation = operation();
        withDefaults(operation, """
                { "request": { "offset": 1 } }
                """);
        JsonNode view = objectMapper.readTree("""
                { "type": "view", "id": "user-list",
                  "properties": { "overrides": { "request": { "offset": 5 } } } }
                """);

        resolver.applyOperationDefaults(operation);
        resolver.applyOverrides(view.get("properties"), operation);

        // 编译阶段最终生效值为 overrides（实际调用值由前端在运行时覆盖，不参与编译）
        assertEquals(5, operation.get("request").get("offset").get("default").asInt(),
                "覆盖默认值应优先于 operation 默认值");
    }

    // --- 1.5.4 defaults 目标字段不存在时报错 ---

    @Test
    void shouldThrowWhenDefaultFieldMissing() {
        JsonNode operation = operation();
        withDefaults(operation, """
                { "request": { "notExists": "x" } }
                """);

        RefNotFoundException e = assertThrows(RefNotFoundException.class,
                () -> resolver.applyOperationDefaults(operation));
        assertTrue(e.getMessage().contains("notExists"), "异常信息应包含目标字段名");
    }

    @Test
    void shouldThrowWhenDefaultSideMissing() {
        JsonNode operation = operation();
        withDefaults(operation, """
                { "request": { "query": { "state": "enable" } }, "response": { "nope": 1 } }
                """);

        // response 存在但字段不存在
        assertThrows(ComposeCompileException.class, () -> resolver.applyOperationDefaults(operation));
    }

    // --- 1.5.5 overrides 类型校验 ---

    @Test
    void shouldRejectOverrideTypeMismatch() {
        JsonNode operation = operation();
        JsonNode function = objectMapper.readTree("""
                { "properties": { "overrides": { "request": { "state": 123 } } } }
                """);

        ComposeCompileException e = assertThrows(ComposeCompileException.class,
                () -> resolver.applyOverrides(function.get("properties"), operation));
        assertTrue(e.getMessage().contains("state"), "异常信息应包含字段名");
    }

    @Test
    void shouldRejectOverrideArrayTypeMismatch() {
        JsonNode operation = operation();
        JsonNode function = objectMapper.readTree("""
                { "properties": { "overrides": { "request": { "sort": "not-an-array" } } } }
                """);

        assertThrows(ComposeCompileException.class,
                () -> resolver.applyOverrides(function.get("properties"), operation));
    }

    @Test
    void shouldAcceptMatchingOverrideType() {
        JsonNode operation = operation();
        JsonNode function = objectMapper.readTree("""
                { "properties": { "overrides": { "request": { "offset": 5 } } } }
                """);

        resolver.applyOverrides(function.get("properties"), operation);

        assertEquals(5, operation.get("request").get("offset").get("default").asInt());
    }
}
