package cn.procsl.ping.boot.ui.component;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 装配归宿规则（spec：装配描述按顶层类型归宿）——
 * 应用树 / 导航片段合并 / 页面片段按 router 挂载 / 无挂载目标不报错。
 */
class UIComponentMountTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    private UIComponentRegistry registry() {
        return new UIComponentRegistry(null);
    }

    private ObjectNode node(String source) {
        try {
            JsonNode parsed = mapper.readTree(source);
            assertTrue(parsed.isObject(), "夹具必须是 JSON 对象");
            return (ObjectNode) parsed;
        } catch (Exception e) {
            throw new IllegalStateException("测试夹具 JSON 无法解析", e);
        }
    }

    private static final String SHELL = """
        {
          "type": "application", "id": "ping.ui.app", "name": "App",
          "containers": [
            {
              "type": "layout", "layout": "left", "id": "layout.left",
              "containers": [
                {"type": "menu", "id": "m.home", "name": "工作台", "router": "home/dashboard", "order": 1},
                {"type": "menu", "id": "m.user", "name": "用户管理", "router": "system/user", "order": 2}
              ]
            },
            {"type": "layout", "layout": "right", "id": "layout.right", "containers": []}
          ]
        }
        """;

    private ObjectNode findById(JsonNode node, String id) {
        if (node.isObject()) {
            if (id.equals(node.path("id").asString(""))) {
                return (ObjectNode) node;
            }
            for (JsonNode child : node.path("containers")) {
                ObjectNode found = findById(child, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private List<String> idsOf(JsonNode containers) {
        List<String> ids = new ArrayList<>();
        for (JsonNode child : containers) {
            ids.add(child.path("id").asString(""));
        }
        return ids;
    }

    /** 顶层 application → 应用树进入 pages，按 id 去重 */
    @Test
    void applicationRootEntersPagesAndIsDeduplicatedById() {
        ObjectNode duplicate = node(SHELL);
        duplicate.put("id", "ping.ui.app");

        List<ObjectNode> result = assertDoesNotThrow(() ->
            registry().mount(List.of(node(SHELL), duplicate)));

        assertEquals(1, result.size(), "同 id 的应用根应去重，后者覆盖");
        assertEquals("application", result.get(0).path("type").asString());
    }

    /** 导航片段按 order 合并进应用外壳树的左侧导航，且同级按 order 升序 */
    @Test
    void navigationFragmentMergesIntoShellInOrder() {
        ObjectNode navGroup = node("""
            {
              "type": "nav_group", "id": "nav.extra", "name": "额外分组", "order": 5,
              "containers": [
                {"type": "menu", "id": "m.extra", "name": "额外菜单", "router": "extra/page", "order": 1}
              ]
            }
            """);

        List<ObjectNode> result = registry().mount(List.of(node(SHELL), navGroup));

        assertEquals(1, result.size(), "导航片段并入外壳后不应成为独立条目");
        ObjectNode left = findById(result.get(0), "layout.left");
        assertNotNull(left, "合并目标应是 layout[left]");
        assertEquals(List.of("m.home", "m.user", "nav.extra"),
            idsOf(left.path("containers")),
            "同级顺序应与 order 升序一致（1、2、5）");
    }

    /** 页面片段按 router 挂载到 router 相同的 menu 节点之下 */
    @Test
    void pageFragmentMountsToMatchingMenu() {
        ObjectNode dataset = node("""
            {
              "type": "dataset", "id": "table.user", "router": "system/user",
              "containers": [
                {"type": "column", "id": "col.account", "field": "account", "name": "账号"}
              ]
            }
            """);

        List<ObjectNode> result = registry().mount(List.of(node(SHELL), dataset));

        assertEquals(1, result.size(), "挂载成功后不应产生独立条目");
        ObjectNode menu = findById(result.get(0), "m.user");
        assertNotNull(menu);
        assertEquals(List.of("table.user"), idsOf(menu.path("containers")),
            "页面片段应挂在 router 相同的 menu 节点之下");
    }

    /** 页面片段没有挂载目标 → 原样进入 pages，且不报错 */
    @Test
    void unmatchedPageFragmentFallsBackIntoPagesWithoutError() {
        ObjectNode orphan = node("""
            {
              "type": "form", "id": "form.orphan", "router": "no/such/menu",
              "containers": []
            }
            """);

        List<ObjectNode> result = assertDoesNotThrow(() ->
            registry().mount(List.of(node(SHELL), orphan)));

        assertEquals(2, result.size(), "无挂载目标时片段必须原样进入 pages");
        ObjectNode last = result.get(result.size() - 1);
        assertEquals("form.orphan", last.path("id").asString());
    }

    /** 没有应用外壳时，导航片段也不报错，原样进入 pages */
    @Test
    void navigationFragmentWithoutShellFallsBackIntoPages() {
        ObjectNode menuFragment = node("""
            {"type": "menu", "id": "m.loose", "name": "游离菜单", "router": "loose/page"}
            """);

        List<ObjectNode> result = assertDoesNotThrow(() ->
            registry().mount(List.of(menuFragment)));

        assertEquals(1, result.size());
        assertEquals("m.loose", result.get(0).path("id").asString());
    }

    /** 页面片段的子树在挂载后不被截断 */
    @Test
    void mountedFragmentKeepsItsSubtree() {
        ObjectNode dataset = node("""
            {
              "type": "dataset", "id": "table.user", "router": "system/user",
              "containers": [
                {"type": "query", "id": "q.user", "containers": [
                  {"type": "field", "id": "f.account", "name": "account", "widget": "input"}
                ]}
              ]
            }
            """);

        List<ObjectNode> result = registry().mount(List.of(node(SHELL), dataset));

        ObjectNode mounted = findById(result.get(0), "table.user");
        assertNotNull(mounted, "挂载后的片段必须保留在树中");
        ObjectNode field = findById(mounted, "f.account");
        assertNotNull(field, "深层子节点不得被丢弃");
        assertEquals("input", field.path("widget").asString());
    }
}
