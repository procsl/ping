package cn.procsl.ping.boot.ui.repository;

import cn.procsl.ping.boot.ui.exception.ComposeParseException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * compose.json 仓储测试（任务 1.3.1）：classpath 扫描、JSON 解析、多文件合并。
 */
class ComposeRepositoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ComposeRepository repository = new ComposeRepository(objectMapper);

    // --- classpath 扫描 ---

    @Test
    void scansComposeFilesFromClasspath() {
        List<JsonNode> nodes = repository.loadAll();

        assertEquals(1, nodes.size());
        JsonNode page = nodes.get(0);
        assertEquals("page", page.get("type").asText());
        assertEquals("user", page.get("id").asText());
        assertEquals(2, page.get("children").size());
        assertEquals("list", page.get("children").get(0).get("id").asText());
        assertEquals("findUsers",
            page.get("children").get(0).get("properties").get("operationId").asText());
    }

    // --- JSON 解析 ---

    @Test
    void validArrayParsesToNodeList() {
        Resource compose = resource("compose-a.json", """
                [
                  { "type": "page", "id": "dashboard" },
                  { "type": "page", "id": "user" }
                ]
                """);

        List<JsonNode> nodes = repository.merge(List.of(compose));

        assertEquals(2, nodes.size());
        assertEquals("dashboard", nodes.get(0).get("id").asText());
        assertEquals("user", nodes.get(1).get("id").asText());
    }

    @Test
    void emptyArrayParsesToEmptyList() {
        List<JsonNode> nodes = repository.merge(List.of(resource("empty.json", "[]")));
        assertTrue(nodes.isEmpty());
    }

    @Test
    void malformedJsonThrowsParseErrorWithFileAndLine() {
        ComposeParseException e = assertThrows(ComposeParseException.class,
            () -> repository.merge(List.of(resource("broken.json", "[ { \"type\": \"page\", } ]"))));

        assertTrue(e.getMessage().contains("broken.json"));
        assertEquals("file:/test/broken.json", e.getFilePath());
        assertTrue(e.getLineNumber() != null && e.getLineNumber() > 0);
    }

    @Test
    void nonArrayRootThrowsParseError() {
        ComposeParseException e = assertThrows(ComposeParseException.class,
            () -> repository.merge(List.of(resource("object.json", "{ \"type\": \"page\" }"))));

        assertTrue(e.getMessage().contains("must be a JSON array"));
        assertTrue(e.getMessage().contains("object.json"));
    }

    @Test
    void nonObjectElementThrowsParseErrorWithIndex() {
        ComposeParseException e = assertThrows(ComposeParseException.class,
            () -> repository.merge(List.of(resource("scalar.json", "[ \"not-an-object\" ]"))));

        assertTrue(e.getMessage().contains("index 0"));
        assertTrue(e.getMessage().contains("must be an object"));
    }

    // --- 多文件合并 ---

    @Test
    void multipleFilesMergedInOrder() {
        Resource fileA = resource("module-a.json", """
                [ { "type": "page", "id": "a-page" } ]
                """);
        Resource fileB = resource("module-b.json", """
                [ { "type": "page", "id": "b-page" }, { "type": "page", "id": "c-page" } ]
                """);

        List<JsonNode> nodes = repository.merge(List.of(fileA, fileB));

        assertEquals(3, nodes.size());
        assertEquals(List.of("a-page", "b-page", "c-page"),
            nodes.stream().map(n -> n.get("id").asText()).toList());
    }

    @Test
    void anyInvalidFileFailsWholeMerge() {
        Resource good = resource("good.json", "[ { \"type\": \"page\", \"id\": \"ok\" } ]");
        Resource broken = resource("bad.json", "not-a-json");

        assertThrows(ComposeParseException.class,
            () -> repository.merge(List.of(good, broken)));
    }

    private Resource resource(String name, String content) {
        return NamedResource.of(name, content);
    }
}
