package cn.procsl.ping.boot.ui.repository;

import cn.procsl.ping.boot.ui.exception.ComposeParseException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OpenAPI 文档仓储测试（任务 1.3.2）：classpath 扫描、解析、多文件合并为文档列表。
 *
 * <p>operationId 索引/冲突/$ref 数据源归 {@code CompileContext}，见其测试。
 */
class OpenApiRepositoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OpenApiRepository repository = new OpenApiRepository(objectMapper);

    // --- classpath 扫描 ---

    @Test
    void scansOpenApiFilesFromClasspath() {
        List<JsonNode> documents = repository.loadDocuments();

        assertEquals(1, documents.size());
        JsonNode doc = documents.get(0);
        assertEquals("3.0.3", doc.get("openapi").asString());
        assertTrue(doc.get("paths").has("/v1/system/users"));
        assertTrue(doc.get("paths").has("/v1/system/users/{id}/account"));
        assertTrue(doc.get("components").has("schemas"));
    }

    // --- 解析 ---

    @Test
    void malformedJsonThrowsParseErrorWithFileAndLine() {
        ComposeParseException e = assertThrows(ComposeParseException.class,
            () -> repository.parseAll(List.of(NamedResource.of("broken.json", "{ not-json"))));

        assertTrue(e.getMessage().contains("broken.json"));
        assertTrue(e.getLineNumber() != null);
    }

    @Test
    void nonObjectRootThrowsParseErrorWithFile() {
        ComposeParseException e = assertThrows(ComposeParseException.class,
            () -> repository.parseAll(List.of(NamedResource.of("array.json", "[]"))));

        assertTrue(e.getMessage().contains("must be a JSON object"));
        assertTrue(e.getMessage().contains("array.json") || e.getFilePath().contains("array.json"));
    }

    @Test
    void emptyDocumentKeptAsSingleDocument() {
        List<JsonNode> documents =
            repository.parseAll(List.of(NamedResource.of("empty.json", "{ \"openapi\": \"3.0.3\" }")));

        assertEquals(1, documents.size());
        assertEquals("3.0.3", documents.get(0).get("openapi").asString());
    }

    // --- 多文件合并 ---

    @Test
    void multipleFilesParseToOrderedDocumentList() {
        List<JsonNode> documents = repository.parseAll(List.of(
            NamedResource.of("openapi-module-a.json", """
                    { "openapi": "3.0.3", "paths": { "/v1/a": { "get": { "operationId": "getA" } } } }
                    """),
            NamedResource.of("openapi-module-b.json", """
                    { "openapi": "3.0.3", "paths": { "/v1/b": { "post": { "operationId": "postB" } } } }
                    """)));

        assertEquals(2, documents.size());
        assertTrue(documents.get(0).get("paths").has("/v1/a"));
        assertTrue(documents.get(1).get("paths").has("/v1/b"));
    }

    @Test
    void anyInvalidFileFailsWholeParse() {
        assertThrows(ComposeParseException.class, () -> repository.parseAll(List.of(
            NamedResource.of("good.json", "{ \"openapi\": \"3.0.3\" }"),
            NamedResource.of("bad.json", "[1,2,3]"))));
    }
}
