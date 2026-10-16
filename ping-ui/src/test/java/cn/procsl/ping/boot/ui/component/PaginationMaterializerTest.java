package cn.procsl.ping.boot.ui.component;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaginationMaterializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaginationMaterializer materializer = new PaginationMaterializer(objectMapper);

    private JsonNode pagedOperation() {
        return objectMapper.readTree("""
                {
                  "method": "GET",
                  "path": "/v1/system/users",
                  "request": {
                    "query": { "type": "object", "properties": { "name": { "type": "string" } } },
                    "offset": { "type": "integer", "default": 1 },
                    "size": { "type": "integer", "default": 10 },
                    "sort": { "type": "array", "items": { "type": "string" } }
                  },
                  "response": {
                    "content": {
                      "type": "array",
                      "items": { "type": "object", "properties": { "name": { "type": "string" }, "gender": { "type": "string" } } }
                    },
                    "total": { "type": "integer" }
                  }
                }
                """);
    }

    private JsonNode plainOperation() {
        return objectMapper.readTree("""
                {
                  "method": "PATCH",
                  "path": "/v1/system/users/{id}",
                  "request": { "body": { "type": "object", "properties": { "state": { "type": "string" } } } },
                  "response": {}
                }
                """);
    }

    private JsonNode view(String propertiesExtra) {
        String properties = propertiesExtra.isEmpty()
                ? "{ \"operation\": \"findUsers\" }"
                : "{ \"operation\": \"findUsers\", " + propertiesExtra + " }";
        return objectMapper.readTree("""
                {
                  "type": "view",
                  "id": "user-list",
                  "properties": %s,
                  "children": [
                    { "type": "input", "id": "f.name", "properties": { "ref": "findUsers.request.query.name" } },
                    { "type": "output", "id": "c.name", "properties": { "ref": "findUsers.response.content[].name" } },
                    { "type": "output", "id": "c.gender", "properties": { "ref": "findUsers.response.content[].gender" } }
                  ]
                }
                """.formatted(properties));
    }

    private JsonNode lastChild(JsonNode view) {
        JsonNode children = view.get("children");
        return children.get(children.size() - 1);
    }

    // --- 1.6.1 分页检测 ---

    @Test
    void shouldDetectPagedOperation() {
        assertTrue(materializer.isPaged(pagedOperation()));
    }

    @Test
    void shouldDetectNonPagedOperation() {
        assertFalse(materializer.isPaged(plainOperation()));
    }

    // --- 1.6.2 pagination 容器生成，children 为 output 子节点 ---

    @Test
    void shouldGeneratePaginationContainerWithOutputChildren() {
        JsonNode view = view("");
        materializer.materialize(view, pagedOperation());

        JsonNode pagination = lastChild(view);
        assertEquals("pagination", pagination.get("type").asString());
        assertEquals("page.user-list", pagination.get("id").asString());
        assertEquals(2, pagination.get("children").size());
        assertEquals("c.name", pagination.get("children").get(0).get("id").asString());
        assertEquals("c.gender", pagination.get("children").get(1).get("id").asString());
    }

    // --- 1.6.3 offset/size input 生成 ---

    @Test
    void shouldGenerateOffsetAndSizeInputs() {
        JsonNode view = view("");
        materializer.materialize(view, pagedOperation());

        JsonNode children = view.get("children");
        JsonNode offset = children.get(children.size() - 3);
        JsonNode size = children.get(children.size() - 2);

        assertEquals("input", offset.get("type").asString());
        assertEquals("page.offset", offset.get("id").asString());
        assertEquals("findUsers.request.offset", offset.get("properties").get("ref").asString());

        assertEquals("input", size.get("type").asString());
        assertEquals("page.size", size.get("id").asString());
        assertEquals("findUsers.request.size", size.get("properties").get("ref").asString());
    }

    // --- 1.6.4 mode 属性：默认 page，支持 scroll ---

    @Test
    void shouldDefaultModeToPage() {
        JsonNode view = view("");
        materializer.materialize(view, pagedOperation());

        assertEquals("page", lastChild(view).get("properties").get("mode").asString());
    }

    @Test
    void shouldSupportScrollMode() {
        JsonNode view = view("\"pagination\": { \"mode\": \"scroll\" }");
        materializer.materialize(view, pagedOperation());

        assertEquals("scroll", lastChild(view).get("properties").get("mode").asString());
    }

    // --- 1.6.5 pagination 插入 view children 末尾（output 之后） ---

    @Test
    void shouldAppendPaginationAtEnd() {
        JsonNode view = view("");
        materializer.materialize(view, pagedOperation());

        JsonNode children = view.get("children");
        assertEquals("input", children.get(0).get("type").asString(), "原 input 应保留");
        assertEquals("pagination", children.get(children.size() - 1).get("type").asString(),
                "pagination 应位于末尾");
        for (JsonNode child : children) {
            assertNotEquals("output", child.get("type").asString(), "output 应被移入 pagination");
        }
    }

    // --- 1.6.6 非分页 operation 不生成 pagination ---

    @Test
    void shouldNotGeneratePaginationForNonPagedOperation() {
        JsonNode view = view("");
        materializer.materialize(view, plainOperation());

        for (JsonNode child : view.get("children")) {
            assertNotEquals("pagination", child.get("type").asString());
        }
        assertEquals(3, view.get("children").size(), "非分页 operation 不应改变 children");
    }

    // --- 集成：pagination 的分页引用与 offset input 复用同一 UUID ---

    @Test
    void shouldResolvePaginationRefsAndReuseUuid() {
        Map<String, JsonNode> operations = new HashMap<>();
        operations.put("findUsers", pagedOperation());
        RefResolver refResolver = new RefResolver(operations);

        JsonNode view = view("");
        materializer.materialize(view, operations.get("findUsers"));
        refResolver.resolveContainer(view);

        JsonNode pagination = lastChild(view);
        String offsetId = operations.get("findUsers").get("request").get("offset").get("id").asString();
        assertEquals(offsetId, pagination.get("properties").get("offset_ref").asString(),
                "pagination.offset_ref 应解析为字段 UUID");

        JsonNode offsetInput = view.get("children").get(view.get("children").size() - 3);
        assertEquals(offsetId, offsetInput.get("properties").get("ref").asString(),
                "pagination.offset_ref 与 page.offset.ref 应复用同一 UUID");
    }
}
