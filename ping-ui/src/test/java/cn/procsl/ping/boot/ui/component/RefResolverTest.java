package cn.procsl.ping.boot.ui.component;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RefResolverTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 编译输入 fixture：operation 索引（已完成 $ref 展开）。
     * 结构取自 design.md 的 findUsers / accountStatusSetting。
     */
    private Map<String, JsonNode> operations() {
        Map<String, JsonNode> map = new HashMap<>();
        map.put("findUsers", objectMapper.readTree("""
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
                      "items": {
                        "type": "object",
                        "properties": {
                          "id": { "type": "integer" },
                          "name": { "type": "string" },
                          "account": {
                            "type": "object",
                            "properties": { "balance": { "type": "integer" } }
                          }
                        }
                      }
                    },
                    "total": { "type": "integer" }
                  }
                }
                """));
        map.put("accountStatusSetting", objectMapper.readTree("""
                {
                  "method": "PATCH",
                  "path": "/v1/system/users/{id}/account",
                  "request": {
                    "path": { "id": { "type": "integer", "format": "int64" } },
                    "body": { "type": "object", "properties": { "state": { "type": "string" } } }
                  },
                  "response": {}
                }
                """));
        return map;
    }

    /** 按 {@code request/query/name}、{@code response/content/items/name} 形式的斜杠路径定位字段 */
    private JsonNode field(Map<String, JsonNode> ops, String operationId, String path) {
        JsonNode node = ops.get(operationId);
        for (String segment : path.split("/")) {
            JsonNode properties = node.get("properties");
            node = properties != null ? properties.get(segment) : node.get(segment);
        }
        return node;
    }

    // --- 5.2.1 openapi:// 协议路径解析：定位到具体字段节点 ---

    @Test
    void shouldResolveRefToFieldNode() {
        Map<String, JsonNode> ops = operations();
        RefResolver resolver = new RefResolver(ops);

        String uuid = resolver.resolve("openapi://findUsers/request/query/name", "findUsers");

        assertNotNull(uuid);
        assertEquals(uuid, field(ops, "findUsers", "request/query/name").get("id").asString(),
                "被引用字段应写入 id");
    }

    @Test
    void shouldResolveNestedAndArrayItemsFields() {
        Map<String, JsonNode> ops = operations();
        RefResolver resolver = new RefResolver(ops);

        String account = resolver.resolve("openapi://findUsers/response/content/items/account", "findUsers");
        String name = resolver.resolve("openapi://findUsers/response/content/items/name", "findUsers");

        assertEquals(account, field(ops, "findUsers", "response/content/items/account").get("id").asString());
        assertEquals(name, field(ops, "findUsers", "response/content/items/name").get("id").asString());
        assertNotEquals(account, name, "不同字段应生成不同 UUID");
    }

    @Test
    void shouldResolveScalarRequestField() {
        Map<String, JsonNode> ops = operations();
        RefResolver resolver = new RefResolver(ops);

        String uuid = resolver.resolve("openapi://findUsers/request/sort", "findUsers");

        assertEquals(uuid, field(ops, "findUsers", "request/sort").get("id").asString());
    }

    @Test
    void shouldResolvePathAndBodyFields() {
        Map<String, JsonNode> ops = operations();
        RefResolver resolver = new RefResolver(ops);

        String pathId = resolver.resolve("openapi://accountStatusSetting/request/path/id", "accountStatusSetting");
        String bodyState = resolver.resolve("openapi://accountStatusSetting/request/body/state", "accountStatusSetting");

        assertEquals(pathId, field(ops, "accountStatusSetting", "request/path/id").get("id").asString());
        assertEquals(bodyState, field(ops, "accountStatusSetting", "request/body/state").get("id").asString());
    }

    // --- 1.4.2 UUID 生成 + 替换（properties.$ref 值替换为 UUID） ---

    @Test
    void shouldReplaceRefWithUuid() {
        Map<String, JsonNode> ops = operations();
        RefResolver resolver = new RefResolver(ops);

        JsonNode input = objectMapper.readTree("""
                { "type": "input", "id": "f.name", "properties": { "$ref": "openapi://findUsers/request/query/name" } }
                """);
        resolver.resolveComponent(input, "findUsers");

        String ref = input.get("properties").get("$ref").asString();
        assertFalse(ref.startsWith(Schemas.REF_PROTOCOL), "ref 应被替换为 UUID");
        assertFalse(ref.contains("/"), "ref 应为纯 UUID");
        assertEquals(ref, field(ops, "findUsers", "request/query/name").get("id").asString(),
                "替换值应与字段 id 一致");
    }

    @Test
    void shouldResolveContainerChildren() {
        Map<String, JsonNode> ops = operations();
        RefResolver resolver = new RefResolver(ops);

        JsonNode view = objectMapper.readTree("""
                {
                  "type": "view", "id": "user-list",
                  "properties": { "operation": "findUsers" },
                  "children": [
                    { "type": "input", "id": "f.name", "properties": { "$ref": "openapi://findUsers/request/query/name" } },
                    { "type": "output", "id": "c.name", "properties": { "$ref": "openapi://findUsers/response/content/items/name" } }
                  ]
                }
                """);
        resolver.resolveContainer(view);

        String nameUuid = field(ops, "findUsers", "request/query/name").get("id").asString();
        String itemNameUuid = field(ops, "findUsers", "response/content/items/name").get("id").asString();
        assertEquals(nameUuid, view.get("children").get(0).get("properties").get("$ref").asString());
        assertEquals(itemNameUuid, view.get("children").get(1).get("properties").get("$ref").asString());
    }

    // --- 5.3.1 引用完整性校验（openapi:// 协议）：断链报错 ---

    @Test
    void shouldThrowRefNotFoundForMissingOperation() {
        RefResolver resolver = new RefResolver(operations());

        RefNotFoundException e = assertThrows(RefNotFoundException.class,
                () -> resolver.resolve("openapi://notExists/request/query/name", "notExists"));

        assertTrue(e.getMessage().contains("openapi://notExists/request/query/name"), "应包含完整 openapi:// 路径");
        assertEquals("openapi://notExists/request/query/name", e.getRef());
    }

    @Test
    void shouldThrowRefNotFoundForMissingField() {
        RefResolver resolver = new RefResolver(operations());

        RefNotFoundException e = assertThrows(RefNotFoundException.class,
                () -> resolver.resolve("openapi://findUsers/request/query/nope", "findUsers"));

        assertTrue(e.getMessage().contains("openapi://findUsers/request/query/nope"));
    }

    @Test
    void shouldThrowRefNotFoundForMissingItemsSchema() {
        RefResolver resolver = new RefResolver(operations());

        // sort 是 array<string>，items 无 properties，深层字段导航应断链报错
        RefNotFoundException e = assertThrows(RefNotFoundException.class,
                () -> resolver.resolve("openapi://findUsers/request/sort/items/name", "findUsers"));

        assertTrue(e.getMessage().contains("openapi://findUsers/request/sort/items/name"));
    }

    // --- 1.4.4 重复 $ref 复用相同 UUID ---

    @Test
    void shouldReuseUuidForSameRef() {
        Map<String, JsonNode> ops = operations();
        RefResolver resolver = new RefResolver(ops);

        String first = resolver.resolve("openapi://findUsers/request/query/name", "findUsers");
        String second = resolver.resolve("openapi://findUsers/request/query/name", "findUsers");

        assertEquals(first, second, "同一字段应复用相同 UUID");
    }

    @Test
    void shouldReuseUuidAcrossComponents() {
        Map<String, JsonNode> ops = operations();
        RefResolver resolver = new RefResolver(ops);

        JsonNode a = objectMapper.readTree("""
                { "type": "input", "id": "a", "properties": { "$ref": "openapi://findUsers/request/query/name" } }
                """);
        JsonNode b = objectMapper.readTree("""
                { "type": "input", "id": "b", "properties": { "$ref": "openapi://findUsers/request/query/name" } }
                """);
        resolver.resolveComponent(a, "findUsers");
        resolver.resolveComponent(b, "findUsers");

        assertEquals(a.get("properties").get("$ref").asString(),
                b.get("properties").get("$ref").asString(), "同一字段的多个组件应复用相同 UUID");
    }

    // --- 1.4.5 跨 operation $ref 检测 ---

    @Test
    void shouldRejectCrossOperationRef() {
        RefResolver resolver = new RefResolver(operations());

        ComposeCompileException e = assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("openapi://findUsers/request/query/name", "accountStatusSetting"));

        assertTrue(e.getMessage().contains("Cross-operation"), "应明确提示跨 operation");
        assertTrue(e.getMessage().contains("openapi://findUsers/request/query/name"));
    }

    @Test
    void shouldRejectCrossOperationRefInContainer() {
        RefResolver resolver = new RefResolver(operations());

        JsonNode form = objectMapper.readTree("""
                {
                  "type": "form", "id": "edit-user",
                  "properties": { "operation": "accountStatusSetting" },
                  "children": [
                    { "type": "input", "id": "f.state", "properties": { "$ref": "openapi://findUsers/request/query/state" } }
                  ]
                }
                """);

        assertThrows(ComposeCompileException.class, () -> resolver.resolveContainer(form));
    }

    // --- 5.2.2 路径格式校验（openapi:// 协议） ---

    @Test
    void shouldRejectInvalidRefFormat() {
        RefResolver resolver = new RefResolver(operations());

        // 缺少 openapi:// 前缀（旧点分格式已移除）
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("findUsers.request.query.name", "findUsers"));
        // 前缀后无内容
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("openapi://", "findUsers"));
        // 段数不足（operation 级 ref 不属于字段路径）
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("openapi://findUsers", "findUsers"));
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("openapi://findUsers/request", "findUsers"));
        // 第二段不是 request/response
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("openapi://findUsers/query/name", "findUsers"));
        // 空路径段
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("openapi://findUsers//request//name", "findUsers"));
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("openapi://findUsers/request/query/", "findUsers"));
        // 空白
        assertThrows(ComposeCompileException.class,
                () -> resolver.resolve("  ", "findUsers"));
    }
}
