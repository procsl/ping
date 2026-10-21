package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import cn.procsl.ping.boot.ui.exception.ComposeException;
import cn.procsl.ping.boot.ui.repository.OpenApiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * openapi 内联阶段测试（任务 1.4.3）：内联、$ref 展开、无残留、2xx 过滤、断链/循环报错。
 *
 * <p>正常路径数据来自测试资源 META-INF/ping/openapi-ui-test.json + compose.json。
 */
class OperationTransformerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final OperationTransformer inliner = new OperationTransformer();

    private CompileContext context;

    @BeforeEach
    void setUp() {
        context = new CompileContext(new OpenApiRepository(mapper).loadDocuments());
    }

    private Component function(String id, String operationId) {
        ObjectNode props = mapper.createObjectNode();
        props.put("operationId", operationId);
        return new Component("function", id, props, null);
    }

    private Component inlineUnderPage(Component function) {
        Component page = new Component("page", "user", null, List.of(function));
        List<Component> out = inliner.process(List.of(page), context);
        return out.get(0).getChildren().get(0);
    }

    // --- 内联 ---

    @Test
    void inlinesWhitelistedKeysForGetOperation() {
        Component result = inlineUnderPage(function("list", "findUsers"));
        JsonNode props = result.getProperties();

        assertEquals("findUsers", props.get("operationId").asString());
        assertEquals("GET", props.get("method").asString());
        assertEquals("/v1/system/users", props.get("path").asString());
        assertEquals(4, props.get("parameters").size());
        assertEquals("query", props.get("parameters").get(0).get("in").asString());
        assertFalse(props.has("requestBody")); // GET 无 requestBody 不生成
        assertFalse(props.has("summary"));     // 白名单外的 operation 字段不带入
    }

    @Test
    void inlinesPatchOperationWithPathParamAndRequestBody() {
        Component result = inlineUnderPage(function("edit", "accountStatusSetting"));
        JsonNode props = result.getProperties();

        assertEquals("PATCH", props.get("method").asString());
        assertEquals("/v1/system/users/{id}/account", props.get("path").asString());
        assertEquals("path", props.get("parameters").get(0).get("in").asString());
        assertTrue(props.has("requestBody"));
        assertEquals("object",
            props.get("requestBody").get("content").get("application/json")
                .get("schema").get("type").asString());
    }

    @Test
    void keepsOnly2xxResponses() {
        JsonNode props = inlineUnderPage(function("list", "findUsers")).getProperties();

        assertTrue(props.get("responses").has("200"));
        assertFalse(props.get("responses").has("400")); // 非 2xx 不序列化
    }

    @Test
    void empty204ResponseKept() {
        JsonNode props = inlineUnderPage(function("edit", "accountStatusSetting")).getProperties();
        assertTrue(props.get("responses").has("204"));
        assertEquals(1, props.get("responses").size());
    }

    // --- $ref 展开 ---

    @Test
    void refFullyExpandedWithoutResidue() {
        JsonNode props = inlineUnderPage(function("list", "findUsers")).getProperties();

        assertFalse(props.toString().contains("$ref"),
            "产物中不应有任何 $ref 残留: " + props);

        // FormatPageUserRecord -> content/total，items -> UserRecord 展开
        JsonNode schema = props.get("responses").get("200")
            .get("content").get("application/json").get("schema");
        assertEquals("object", schema.get("type").asString());
        assertTrue(schema.get("properties").has("content"));
        assertTrue(schema.get("properties").has("total"));
        JsonNode item = schema.get("properties").get("content").get("items");
        assertEquals("object", item.get("type").asString());
        assertEquals("integer", item.get("properties").get("id").get("type").asString());
        assertEquals("string", item.get("properties").get("name").get("type").asString());
    }

    @Test
    void sameOperationIdReferencedByMultipleFunctionsIsolated() {
        Component fnA = function("list", "findUsers");
        Component fnB = function("overview", "findUsers");

        List<Component> out = inliner.process(List.of(
            new Component("page", "user", null, List.of(fnA)),
            new Component("page", "dashboard", null, List.of(fnB))), context);

        JsonNode propsA = out.get(0).getChildren().get(0).getProperties();
        JsonNode propsB = out.get(1).getChildren().get(0).getProperties();

        assertNotSame(propsA, propsB); // 接受冗余：两份独立镜像
        assertEquals(propsA, propsB);
    }

    @Test
    void nonFunctionNodeDeclarationUntouched() {
        ObjectNode pageProps = mapper.createObjectNode();
        pageProps.put("custom", "kept");
        Component page = new Component("page", "user", pageProps,
            List.of(function("list", "findUsers")));

        List<Component> out = inliner.process(List.of(page), context);

        assertEquals("page", out.get(0).getType());
        assertEquals(pageProps, out.get(0).getProperties());
        assertEquals("GET",
            out.get(0).getChildren().get(0).getProperties().get("method").asString());
    }

    // --- 失败场景（启动失败，D8） ---

    @Test
    void brokenOperationRefReportsPathAndOrigin() {
        Component page = new Component("page", "user", null,
            List.of(function("list", "noSuchOperation")));

        ComposeException e = assertThrows(ComposeException.class,
            () -> inliner.process(List.of(page), context));

        assertTrue(e.getMessage().contains("noSuchOperation"));
        assertTrue(e.getMessage().contains("not found in OpenAPI document"));
        assertTrue(e.getMessage().contains("page 'user' function 'list'"),
            "报错应含声明来源路径: " + e.getMessage());
    }

    @Test
    void functionWithoutOperationIdReportsError() {
        Component invalid = new Component("function", "broken", mapper.createObjectNode(), null);
        Component page = new Component("page", "user", null, List.of(invalid));

        ComposeException e = assertThrows(ComposeException.class,
            () -> inliner.process(List.of(page), context));

        assertTrue(e.getMessage().contains("Missing 'properties.operationId'"));
        assertTrue(e.getMessage().contains("page 'user' function 'broken'"));
    }

    @Test
    void circularSchemaRefReportsPath() throws Exception {
        CompileContext loopContext = new CompileContext(List.of(mapper.readTree("""
                {
                  "paths": {
                    "/v1/loop": {
                      "get": {
                        "operationId": "loopOp",
                        "responses": {
                          "200": {
                            "content": {
                              "application/json": {
                                "schema": { "$ref": "#/components/schemas/A" }
                              }
                            }
                          }
                        }
                      }
                    }
                  },
                  "components": {
                    "schemas": {
                      "A": { "type": "object", "properties": { "b": { "$ref": "#/components/schemas/B" } } },
                      "B": { "type": "object", "properties": { "a": { "$ref": "#/components/schemas/A" } } }
                    }
                  }
                }
                """)));

        Component page = new Component("page", "loop", null, List.of(function("run", "loopOp")));

        ComposeException e = assertThrows(ComposeException.class,
            () -> inliner.process(List.of(page), loopContext));

        assertTrue(e.getMessage().contains("Circular $ref detected"));
        assertTrue(e.getMessage().contains("#/components/schemas/A"));
    }

    @Test
    void missingRefTargetReportsError() throws Exception {
        CompileContext brokenContext = new CompileContext(List.of(mapper.readTree("""
                {
                  "paths": {
                    "/v1/x": {
                      "get": {
                        "operationId": "brokenOp",
                        "responses": {
                          "200": {
                            "content": {
                              "application/json": {
                                "schema": { "$ref": "#/components/schemas/Missing" }
                              }
                            }
                          }
                        }
                      }
                    }
                  },
                  "components": { "schemas": { "Other": { "type": "object" } } }
                }
                """)));

        Component page = new Component("page", "p", null, List.of(function("run", "brokenOp")));

        ComposeException e = assertThrows(ComposeException.class,
            () -> inliner.process(List.of(page), brokenContext));

        assertTrue(e.getMessage().contains("Broken $ref '#/components/schemas/Missing'"));
    }

    @Test
    void emptyTreeInputYieldsEmptyOutput() {
        assertTrue(inliner.process(List.of(), context).isEmpty());
    }

    @Test
    void unsupportedExternalRefReportsError() throws Exception {
        CompileContext externalContext = new CompileContext(List.of(mapper.readTree("""
                {
                  "paths": {
                    "/v1/y": {
                      "get": {
                        "operationId": "extOp",
                        "responses": {
                          "200": {
                            "content": {
                              "application/json": {
                                "schema": { "$ref": "other.json#/components/schemas/X" }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """)));

        Component page = new Component("page", "p", null, List.of(function("run", "extOp")));

        ComposeException e = assertThrows(ComposeException.class,
            () -> inliner.process(List.of(page), externalContext));
        assertTrue(e.getMessage().contains("only local references"));
    }

    @Test
    void indexConstructibleForIsolatedTesting() {
        // CompileContext 单文档直构路径（与仓储加载路径等价）
        assertTrue(context.has("findUsers"));
        assertEquals(Map.of("findUsers", "GET /v1/system/users").get("findUsers"),
            context.operation("findUsers").method() + " " + context.operation("findUsers").path());
    }
}
