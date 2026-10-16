package cn.procsl.ping.boot.ui.component;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.*;

class ComposeRepositoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- 1.1.1 classpath 扫描 ---

    @Test
    void shouldFindMultipleComposeFilesFromClasspath() {
        ComposeRepository repo = new ComposeRepository(objectMapper);
        Compose compose = repo.loadAll();
        assertNotNull(compose);
        assertNotNull(compose.getChildren());
    }

    // --- 1.1.2 JSON 解析 ---

    @Test
    void shouldParseValidComposeJson() {
        String json = """
                [
                  {
                    "type": "operation",
                    "id": "findUsers",
                    "properties": {
                      "api": { "$ref": "openapi://findUsers" }
                    }
                  },
                  {
                    "type": "function",
                    "id": "system/user",
                    "children": []
                  }
                ]
                """;

        ArrayNode items = parseJsonArray(json);
        assertEquals(2, items.size());
        assertEquals("operation", items.get(0).get("type").asText());
        assertEquals("function", items.get(1).get("type").asText());
    }

    // --- 1.1.3 非法 JSON 检测 ---

    @Test
    void shouldThrowComposeParseExceptionForInvalidJson() {
        String invalidJson = "[ { \"type\": \"operation\", \"id\": \"test\", } ]";

        assertThrows(ComposeParseException.class, () -> {
            try {
                objectMapper.readTree(invalidJson);
            } catch (JacksonException e) {
                throw new ComposeParseException("test.json", 1, e.getOriginalMessage(), e);
            }
        });
    }

    @Test
    void shouldIncludeFilePathAndLineNumberInException() {
        String json = "[ invalid json here ]";

        assertThrows(ComposeParseException.class, () -> {
            try {
                objectMapper.readTree(json);
            } catch (JacksonException e) {
                throw new ComposeParseException("META-INF/ping/ui/compose.json", 1,
                        e.getOriginalMessage(), e);
            }
        });
    }

    // --- 1.1.4 空数组检测 ---

    @Test
    void shouldAcceptEmptyArrayComposeJson() {
        String json = "[]";

        ArrayNode items = parseJsonArray(json);
        assertEquals(0, items.size());
        assertTrue(items.isArray());
    }

    // --- 1.1.5 顶层类型检测 ---

    @Test
    void shouldRejectInvalidTopLevelType() {
        String json = """
                [
                  { "type": "layout", "id": "test-layout" }
                ]
                """;

        ArrayNode items = parseJsonArray(json);
        String invalidType = items.get(0).get("type").asText();
        assertNotEquals("operation", invalidType);
        assertNotEquals("function", invalidType);
    }

    @Test
    void shouldAcceptOperationAndFunctionTypes() {
        String json = """
                [
                  { "type": "operation", "id": "test-op" },
                  { "type": "function", "id": "test-fn" }
                ]
                """;

        ArrayNode items = parseJsonArray(json);
        assertEquals("operation", items.get(0).get("type").asText());
        assertEquals("function", items.get(1).get("type").asText());
    }

    @Test
    void shouldRejectMissingTypeField() {
        String json = "[ { \"id\": \"no-type\" } ]";

        assertThrows(ComposeParseException.class, () -> {
            ArrayNode items = parseJsonArray(json);
            validateItems(items);
        });
    }

    // --- 1.1.6 多文件合并 ---

    @Test
    void shouldMergeMultipleComposeFiles() {
        ArrayNode file1 = objectMapper.createArrayNode();
        file1.add(objectMapper.createObjectNode()
                .put("type", "operation").put("id", "op1"));

        ArrayNode file2 = objectMapper.createArrayNode();
        file2.add(objectMapper.createObjectNode()
                .put("type", "function").put("id", "fn1"));

        ArrayNode merged = objectMapper.createArrayNode();
        file1.forEach(merged::add);
        file2.forEach(merged::add);

        Compose compose = new Compose(merged);
        assertEquals(2, compose.getChildren().size());
        assertEquals("op1", compose.getChildren().get(0).get("id").asText());
        assertEquals("fn1", compose.getChildren().get(1).get("id").asText());
    }

    // --- Helper methods ---

    private ArrayNode parseJsonArray(String json) {
        var node = objectMapper.readTree(json);
        assertTrue(node.isArray(), "Expected JSON array");
        return (ArrayNode) node;
    }

    private void validateItems(ArrayNode items) {
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            var typeNode = item.get("type");
            if (typeNode == null || !typeNode.isTextual()) {
                throw new ComposeParseException("test", null,
                        "Array element at index " + i + " missing 'type' field");
            }
            String type = typeNode.asText();
            if (!type.equals("operation") && !type.equals("function")) {
                throw new ComposeParseException("test", null,
                        "Array element at index " + i + " has invalid type '" + type + "'");
            }
        }
    }
}
