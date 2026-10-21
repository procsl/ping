package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.exception.ComposeException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 编译上下文测试（任务 1.4.2）：整文档存储 + operationId 索引读写 + 冲突规则。
 */
class CompileContextTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode document(String pathsJson) {
        return mapper.readTree("{ \"openapi\": \"3.0.3\", \"paths\": " + pathsJson + " }");
    }

    @Test
    void storesFullOpenApiDocuments() throws Exception {
        JsonNode docA = document("{ \"/v1/a\": { \"get\": { \"operationId\": \"getA\" } } }");
        JsonNode docB = document("{ \"/v1/b\": { \"post\": { \"operationId\": \"postB\" } } }");

        CompileContext context = new CompileContext(List.of(docA, docB));

        assertEquals(2, context.documents().size());
        assertTrue(context.documents().getFirst().has("openapi"));
    }

    @Test
    void injectsMethodAndPathIntoIndex() {
        CompileContext context = new CompileContext(List.of(document(
            "{ \"/v1/system/users\": { \"get\": { \"operationId\": \"findUsers\", \"parameters\": [] } } }")));

        CompileContext.Operation op = context.operation("findUsers");
        assertEquals("GET", op.method());
        assertEquals("/v1/system/users", op.path());
        assertEquals("GET", op.definition().get("method").asString());
        assertEquals("findUsers", op.definition().get("operationId").asString());
        assertTrue(context.has("findUsers"));
    }

    @Test
    void indexEntryCarriesSourceDocumentForRefExpansion() {
        JsonNode source = document(
            "{ \"/v1/a\": { \"get\": { \"operationId\": \"getA\", \"responses\": {"
                + " \"200\": { \"content\": { \"application/json\": {"
                + " \"schema\": { \"$ref\": \"#/components/schemas/X\" } } } } } } } }");
        // 手工补 components
        ((ObjectNode) source).putObject("components")
            .putObject("schemas")
            .putObject("X")
            .put("type", "object");

        CompileContext context = new CompileContext(List.of(source));
        CompileContext.Operation op = context.operation("getA");

        assertEquals(source, op.sourceDocument());
        JsonNode schemas = op.schemas();
        assertTrue(schemas.has("X"));
        assertEquals("object", schemas.get("X").get("type").asString());
    }

    @Test
    void schemasReturnsNullWithoutComponents() {
        CompileContext context = new CompileContext(List.of(document(
            "{ \"/v1/a\": { \"get\": { \"operationId\": \"getA\" } } }")));

        assertNull(context.operation("getA").schemas());
    }

    @Test
    void mergesIndexAcrossDocuments() {
        CompileContext context = new CompileContext(List.of(
            document("{ \"/v1/a\": { \"get\": { \"operationId\": \"getA\" } } }"),
            document("{ \"/v1/b\": { \"get\": { \"operationId\": \"getB\" } } }")));

        assertEquals(2, context.operations().size());
        assertTrue(context.has("getA"));
        assertTrue(context.has("getB"));
    }

    @Test
    void crossDocumentOperationIdConflictFailsFast() {
        ComposeException e = assertThrows(ComposeException.class,
            () -> new CompileContext(List.of(
                document("{ \"/v1/a\": { \"get\": { \"operationId\": \"sameId\" } } }"),
                document("{ \"/v1/b\": { \"post\": { \"operationId\": \"sameId\" } } }"))));

        assertTrue(e.getMessage().contains("Conflicting operationId 'sameId'"));
        assertTrue(e.getMessage().contains("GET /v1/a"));
        assertTrue(e.getMessage().contains("POST /v1/b"));
    }

    @Test
    void identicalDefinitionAcrossDocumentsTolerated() {
        String paths = "{ \"/v1/a\": { \"get\": { \"operationId\": \"getA\", \"responses\": {} } } }";

        CompileContext context = new CompileContext(List.of(document(paths), document(paths)));

        assertEquals(1, context.operations().size());
    }

    @Test
    void operationWithoutOperationIdSkipped() {
        CompileContext context = new CompileContext(List.of(document(
            "{ \"/v1/a\": { \"get\": { \"summary\": \"no id\" } } }")));

        assertFalse(context.has(""));
        assertTrue(context.operations().isEmpty());
    }

    @Test
    void nonHttpMethodKeysIgnored() {
        CompileContext context = new CompileContext(List.of(document(
            "{ \"/v1/x\": {"
                + " \"summary\": \"path summary\","
                + " \"parameters\": [ { \"name\": \"common\", \"in\": \"query\" } ],"
                + " \"get\": { \"operationId\": \"getX\" }"
                + " } }")));

        assertEquals(1, context.operations().size());
        assertEquals("GET", context.operation("getX").method());
    }

    @Test
    void documentWithoutPathsYieldsEmptyIndex() {
        CompileContext context = new CompileContext(List.of(mapper.readTree("{ \"openapi\": \"3.0.3\" }")));

        assertTrue(context.documents().size() == 1);
        assertTrue(context.operations().isEmpty());
    }

    @Test
    void unknownOperationIdReturnsNullForBrokenRefError() {
        CompileContext context = new CompileContext(List.of());

        assertFalse(context.has("missing"));
        assertNull(context.operation("missing"));
        assertEquals(0, context.documents().size());
        assertEquals(Map.of(), context.operations());
    }
}
