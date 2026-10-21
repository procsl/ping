package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import cn.procsl.ping.boot.ui.repository.ComposeRepository;
import cn.procsl.ping.boot.ui.repository.OpenApiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主编排器测试（任务 1.4.4）：转换 → 链式变换 → assemble app 根的完整编译流程。
 *
 * <p>正常路径输入来自测试资源 META-INF/ping/ui/compose.json + openapi-ui-test.json。
 */
class ComposeCompilerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private CompileContext context;
    private List<JsonNode> source;

    @BeforeEach
    void setUp() {
        context = new CompileContext(new OpenApiRepository(mapper).loadDocuments());
        source = new ComposeRepository(mapper).loadAll();
    }

    private ComposeCompiler compiler() {
        return new ComposeCompiler(List.of(new OperationTransformer()));
    }

    /** readTree 返回数组节点，展开为顶层元素列表（与 ComposeRepository.loadAll() 形态一致）。 */
    private List<JsonNode> rawArray(String json) throws Exception {
        JsonNode array = mapper.readTree(json);
        List<JsonNode> items = new java.util.ArrayList<>();
        for (JsonNode item : array) {
            items.add(item);
        }
        return items;
    }

    // --- 完整编译流程 ---

    @Test
    void compilesComposeAndOpenApiToAppRootEndToEnd() {
        Component app = compiler().compile(source, context);

        assertEquals("app", app.getType());
        assertEquals("ping.app", app.getId());
        assertEquals(1, app.getChildren().size());

        Component page = app.getChildren().get(0);
        assertEquals("page", page.getType());
        assertEquals("user", page.getId());
        assertEquals(2, page.getChildren().size());

        Component list = page.getChildren().get(0);
        assertEquals("function", list.getType());
        assertEquals("list", list.getId());
        JsonNode props = list.getProperties();
        assertEquals("findUsers", props.get("operationId").asString());
        assertEquals("GET", props.get("method").asString());
        assertEquals("/v1/system/users", props.get("path").asString());
        assertFalse(props.toString().contains("$ref"));

        Component edit = page.getChildren().get(1);
        assertEquals("edit", edit.getId());
        assertEquals("PATCH", edit.getProperties().get("method").asString());
    }

    @Test
    void emptyInputYieldsEmptyAppTree() {
        Component app = compiler().compile(List.of(), context);

        assertEquals("app", app.getType());
        assertTrue(app.getChildren().isEmpty());
    }

    @Test
    void sourceStructureAssembledUntouchedWithoutTransformer() {
        Component app = new ComposeCompiler(List.of()).compile(source, context);

        assertEquals("app", app.getType());
        assertEquals(1, app.getChildren().size());
        Component page = app.getChildren().get(0);
        assertEquals("user", page.getId());
        // 未执行内联：function.properties 仍是源声明（仅 operationId）
        JsonNode props = page.getChildren().get(0).getProperties();
        assertTrue(props.has("operationId"));
        assertFalse(props.has("method"));
    }

    @Test
    void transformersRunInRegistrationOrder() {
        // 第一个阶段注入 page，第二个阶段删除 page：结果为空（验证链式传递）
        Transformer addPage = (nodes, ctx) -> {
            List<Component> result = new java.util.ArrayList<>(nodes);
            result.add(new Component("page", "injected", null, List.of()));
            return result;
        };
        Transformer dropInjected = (nodes, ctx) -> nodes.stream()
            .filter(n -> !n.getId().equals("injected"))
            .toList();

        Component app = new ComposeCompiler(List.of(addPage, dropInjected)).compile(List.of(), context);
        assertTrue(app.getChildren().isEmpty());

        Component app2 = new ComposeCompiler(List.of(dropInjected, addPage)).compile(List.of(), context);
        assertEquals(1, app2.getChildren().size());
        assertEquals("injected", app2.getChildren().get(0).getId());
    }

    // --- 源转换规则 ---

    @Test
    void unknownTypeNodesIgnored() throws Exception {
        List<JsonNode> raw = rawArray("""
                [
                  { "type": "operation", "id": "legacy-op" },
                  { "type": "view", "id": "legacy-view" },
                  { "type": "page", "id": "ok" }
                ]
                """);

        Component app = new ComposeCompiler(List.of()).compile(raw, context);

        assertEquals(1, app.getChildren().size());
        assertEquals("ok", app.getChildren().get(0).getId());
    }

    @Test
    void nodesWithoutTypeOrNonObjectIgnored() throws Exception {
        List<JsonNode> raw = rawArray("""
                [
                  { "id": "no-type" },
                  "not-an-object",
                  { "type": "page", "id": "ok" }
                ]
                """);

        Component app = new ComposeCompiler(List.of()).compile(raw, context);

        assertEquals(1, app.getChildren().size());
        assertEquals("ok", app.getChildren().get(0).getId());
    }

    @Test
    void appTypeInSourceTreatedAsUnknown() throws Exception {
        List<JsonNode> raw = rawArray("""
                [ { "type": "app", "id": "sneaky" } ]
                """);

        Component app = new ComposeCompiler(List.of()).compile(raw, context);

        assertEquals("app", app.getType());
        assertTrue(app.getChildren().isEmpty());
    }

    @Test
    void nonObjectPropertiesTreatedAsAbsent() throws Exception {
        List<JsonNode> raw = rawArray("""
                [ { "type": "page", "id": "p", "properties": "not-an-object" } ]
                """);

        Component app = new ComposeCompiler(List.of()).compile(raw, context);

        assertTrue(app.getChildren().get(0).getProperties() == null);
    }

    // --- id 唯一性校验（任务 2.1 / 2.2） ---

    @Test
    void duplicatePageIdFailsFast() throws Exception {
        List<JsonNode> raw = rawArray("""
                [
                  { "type": "page", "id": "user" },
                  { "type": "page", "id": "dashboard" },
                  { "type": "page", "id": "user" }
                ]
                """);

        cn.procsl.ping.boot.ui.exception.ComposeException e =
            org.junit.jupiter.api.Assertions.assertThrows(
                cn.procsl.ping.boot.ui.exception.ComposeException.class,
                () -> new ComposeCompiler(List.of()).compile(raw, context));

        assertTrue(e.getMessage().contains("Duplicate page id 'user'"));
        assertTrue(e.getMessage().contains("globally unique"));
    }

    @Test
    void duplicateFunctionIdWithinPageFailsFast() throws Exception {
        List<JsonNode> raw = rawArray("""
                [
                  { "type": "page", "id": "user",
                    "children": [
                      { "type": "function", "id": "list",
                        "properties": { "operationId": "findUsers" } },
                      { "type": "function", "id": "list",
                        "properties": { "operationId": "accountStatusSetting" } }
                    ] }
                ]
                """);

        cn.procsl.ping.boot.ui.exception.ComposeException e =
            org.junit.jupiter.api.Assertions.assertThrows(
                cn.procsl.ping.boot.ui.exception.ComposeException.class,
                () -> new ComposeCompiler(List.of()).compile(raw, context));

        assertTrue(e.getMessage().contains("Duplicate function id 'list'"));
        assertTrue(e.getMessage().contains("page 'user'"));
    }

    @Test
    void sameFunctionIdAcrossPagesAllowed() throws Exception {
        List<JsonNode> raw = rawArray("""
                [
                  { "type": "page", "id": "user",
                    "children": [ { "type": "function", "id": "list",
                      "properties": { "operationId": "findUsers" } } ] },
                  { "type": "page", "id": "dashboard",
                    "children": [ { "type": "function", "id": "list",
                      "properties": { "operationId": "findUsers" } } ] }
                ]
                """);

        Component app = compiler().compile(raw, context);

        assertEquals(2, app.getChildren().size());
        assertEquals("list", app.getChildren().get(0).getChildren().get(0).getId());
        assertEquals("list", app.getChildren().get(1).getChildren().get(0).getId());
    }

    @Test
    void nodesWithoutIdSkipUniquenessCheck() throws Exception {
        List<JsonNode> raw = rawArray("""
                [
                  { "type": "page" },
                  { "type": "page" }
                ]
                """);

        Component app = new ComposeCompiler(List.of()).compile(raw, context);
        assertEquals(2, app.getChildren().size());
    }

    // --- 路由生成（任务 2.6） ---

    @Test
    void routeConcatenatedFromPageAndFunctionId() {
        Component app = compiler().compile(source, context);

        Component page = app.getChildren().get(0);
        assertEquals("user/list",
            page.getChildren().get(0).getProperties().get("route").asString());
        assertEquals("user/edit",
            page.getChildren().get(1).getProperties().get("route").asString());
    }

    @Test
    void topLevelFunctionRouteIsOwnId() throws Exception {
        List<JsonNode> raw = rawArray("""
                [
                  { "type": "function", "id": "standalone",
                    "properties": { "operationId": "findUsers" } }
                ]
                """);

        Component app = compiler().compile(raw, context);

        assertEquals("standalone",
            app.getChildren().get(0).getProperties().get("route").asString());
    }

    // --- 未知属性 / 未知类型忽略（任务 2.5） ---

    @Test
    void unknownFunctionPropertiesIgnoredSilently() throws Exception {
        List<JsonNode> raw = rawArray("""
                [
                  { "type": "page", "id": "user",
                    "children": [ { "type": "function", "id": "list",
                      "properties": { "operationId": "findUsers", "futureIntent": "sortable" } } ] }
                ]
                """);

        Component app = compiler().compile(raw, context);

        JsonNode props = app.getChildren().get(0).getChildren().get(0).getProperties();
        assertFalse(props.has("futureIntent")); // 未知属性忽略，不进产物
        assertTrue(props.has("method"));        // 正常内联不受影响
    }

    @Test
    void structuralCompileErrorsPropagate() throws Exception {
        List<JsonNode> raw = rawArray("""
                [
                  { "type": "page", "id": "user",
                    "children": [ { "type": "function", "id": "x",
                      "properties": { "operationId": "missing-op" } } ] }
                ]
                """);

        cn.procsl.ping.boot.ui.exception.ComposeException e =
            org.junit.jupiter.api.Assertions.assertThrows(
                cn.procsl.ping.boot.ui.exception.ComposeException.class,
                () -> compiler().compile(raw, context));

        assertTrue(e.getMessage().contains("missing-op"));
        assertTrue(e.getMessage().contains("page 'user' function 'x'"));
    }
}
