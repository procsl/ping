package cn.procsl.ping.boot.ui.component;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class SchemaRefExpanderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 编译输入 fixture：含一层 $ref、嵌套 $ref、数组 items $ref、循环引用四类 schema */
    private JsonNode document() {
        return objectMapper.readTree("""
                {
                  "components": {
                    "schemas": {
                      "UserQuery": {
                        "type": "object",
                        "properties": {
                          "name": { "type": "string" },
                          "state": { "type": "string", "enum": ["disable", "enable"] }
                        }
                      },
                      "UserAccountRecord": {
                        "type": "object",
                        "properties": { "balance": { "type": "integer" } }
                      },
                      "UserRecord": {
                        "type": "object",
                        "properties": {
                          "id": { "type": "integer" },
                          "account": { "$ref": "#/components/schemas/UserAccountRecord" }
                        }
                      },
                      "FormatPageUserRecord": {
                        "type": "object",
                        "properties": {
                          "content": {
                            "type": "array",
                            "items": { "$ref": "#/components/schemas/UserRecord" }
                          },
                          "total": { "type": "integer" }
                        }
                      },
                      "Node": {
                        "type": "object",
                        "properties": { "child": { "$ref": "#/components/schemas/Node" } }
                      }
                    }
                  }
                }
                """);
    }

    private SchemaRefExpander expander() {
        return new SchemaRefExpander(objectMapper, document());
    }

    // --- 1.3.1 一层 $ref 展开 ---

    @Test
    void shouldExpandSingleLevelRef() {
        JsonNode schema = objectMapper.readTree("""
                { "$ref": "#/components/schemas/UserQuery" }
                """);

        JsonNode expanded = expander().expand(schema);

        assertFalse(expanded.has("$ref"), "展开后不应残留 $ref");
        assertEquals("object", expanded.get("type").asString());
        assertEquals("string", expanded.get("properties").get("name").get("type").asString());
        assertEquals("disable", expanded.get("properties").get("state").get("enum").get(0).asString());
    }

    // --- 1.3.2 嵌套 $ref 展开 ---

    @Test
    void shouldExpandNestedRef() {
        JsonNode schema = objectMapper.readTree("""
                { "$ref": "#/components/schemas/FormatPageUserRecord" }
                """);

        JsonNode expanded = expander().expand(schema);

        JsonNode item = expanded.get("properties").get("content").get("items");
        assertFalse(item.has("$ref"), "数组 items 的 $ref 应被展开");
        assertEquals("object", item.get("type").asString());

        JsonNode account = item.get("properties").get("account");
        assertFalse(account.has("$ref"), "嵌套的 account $ref 也应被展开");
        assertEquals("object", account.get("type").asString());
        assertEquals("integer",
                account.get("properties").get("balance").get("type").asString());
    }

    // --- 1.3.3 循环引用检测 ---

    @Test
    void shouldThrowCircularReferenceException() {
        JsonNode schema = objectMapper.readTree("""
                { "$ref": "#/components/schemas/Node" }
                """);

        CircularReferenceException e = assertThrows(CircularReferenceException.class,
                () -> expander().expand(schema));

        assertTrue(e.getMessage().contains("Node"), "异常信息应包含循环引用的 schema 名");
        assertTrue(e.getResolutionPath().contains("#/components/schemas/Node"),
                "应记录解析路径便于定位");
    }

    @Test
    void shouldDetectIndirectCycle() {
        JsonNode doc = objectMapper.readTree("""
                {
                  "components": { "schemas": {
                    "A": { "type": "object", "properties": { "b": { "$ref": "#/components/schemas/B" } } },
                    "B": { "type": "object", "properties": { "a": { "$ref": "#/components/schemas/A" } } }
                  } }
                }
                """);
        JsonNode schema = objectMapper.readTree("""
                { "$ref": "#/components/schemas/A" }
                """);

        assertThrows(CircularReferenceException.class,
                () -> new SchemaRefExpander(objectMapper, doc).expand(schema));
    }

    // --- 1.3.4 $ref 目标不存在时报错 ---

    @Test
    void shouldThrowWhenRefTargetMissing() {
        JsonNode schema = objectMapper.readTree("""
                { "$ref": "#/components/schemas/DoesNotExist" }
                """);

        assertThrows(ComposeCompileException.class, () -> expander().expand(schema));
    }

    // --- 1.3.5 array items 中的 $ref 展开 ---

    @Test
    void shouldExpandRefInArrayItems() {
        JsonNode schema = objectMapper.readTree("""
                {
                  "type": "array",
                  "items": { "$ref": "#/components/schemas/UserRecord" }
                }
                """);

        JsonNode expanded = expander().expand(schema);

        JsonNode item = expanded.get("items");
        assertFalse(item.has("$ref"), "items 的 $ref 应被展开");
        assertEquals("object", item.get("type").asString());
        assertEquals("integer", item.get("properties").get("id").get("type").asString());
    }

    // --- 同级属性合并（parameter 级 required 标记） ---

    @Test
    void shouldMergeSiblingProperties() {
        JsonNode schema = objectMapper.readTree("""
                { "$ref": "#/components/schemas/UserQuery", "required": true }
                """);

        JsonNode expanded = expander().expand(schema);

        assertTrue(expanded.get("required").asBoolean(false), "同级属性应合并到展开结果");
        assertEquals("object", expanded.get("type").asString());
    }

    // --- 不修改输入树 ---

    @Test
    void shouldNotMutateInput() {
        JsonNode schema = objectMapper.readTree("""
                { "$ref": "#/components/schemas/UserQuery" }
                """);

        expander().expand(schema);

        assertTrue(schema.has("$ref"), "输入树不应被修改");
    }
}
