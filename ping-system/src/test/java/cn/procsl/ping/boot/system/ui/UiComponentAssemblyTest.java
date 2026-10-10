package cn.procsl.ping.boot.system.ui;

import cn.procsl.ping.boot.system.TestSystemApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 跨模块装配集成验证：ping-system 的 compose.json 与 ping-ui 的应用外壳
 * 经构建期聚合 + 启动期装配后，由 GET /v1/ui/components 统一下发；
 * 同时确认旧的 GET /v1/system/menus 已下线。
 *
 * <p>{@code authenticates-prefix} 采用与 {@code ping-distribute/application.properties} 一致的值：
 * 认证策略不在本变更范围内，这里只保证被测接口在与生产一致的前缀配置下可达。
 */
@SpringBootTest(
    classes = TestSystemApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = "procsl.ping.boot.system.authenticates-prefix=/v1/system")
class UiComponentAssemblyTest {

    static final Set<String> SPEC_TYPES = Set.of(
        "application", "layout", "menu", "nav_group", "tabs", "breadcrumb",
        "dataset", "query", "column", "row_action",
        "form", "field", "form_action",
        "action", "placeholder", "empty"
    );

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Autowired
    WebApplicationContext context;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    private JsonNode bodyOf(MvcResult result) {
        try {
            return mapper.readTree(result.getResponse().getContentAsString());
        } catch (Exception e) {
            throw new IllegalStateException("响应不是合法 JSON", e);
        }
    }

    private boolean containsId(JsonNode node, String id) {
        if (node.isObject()) {
            if (id.equals(node.path("id").asString(""))) {
                return true;
            }
            for (JsonNode child : node.path("containers")) {
                if (containsId(child, id)) {
                    return true;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                if (containsId(item, id)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void componentsEndpointExposesTypesAndAssembledPages() throws Exception {
        MvcResult result = mockMvc().perform(get("/v1/ui/components"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();

        JsonNode body = bodyOf(result);

        // types 含 spec 声明的 16 个常用类型
        Set<String> types = new HashSet<>();
        body.path("types").forEach(type -> types.add(type.path("type").asString()));
        Set<String> missing = new HashSet<>(SPEC_TYPES);
        missing.removeAll(types);
        assertTrue(missing.isEmpty(), "types 缺少常用组件类型：" + missing);

        // pages 首项为应用外壳，且 user-center 页面树已被挂载进来
        assertTrue(body.path("pages").isArray() && body.path("pages").size() > 0,
            "pages 不能为空");
        assertEquals("application", body.path("pages").get(0).path("type").asString(),
            "pages[0] 应是应用外壳树");
        assertEquals("ping.ui.app", body.path("pages").get(0).path("id").asString());
        assertTrue(containsId(body.path("pages"), "dataset.user-center"),
            "pages 中应包含 user-center 页面树（挂载在 router 为 system/user 的菜单下）");
    }

    @Test
    void userCenterDatasetCarriesResolvedOrRawApiReference() throws Exception {
        MvcResult result = mockMvc().perform(get("/v1/ui/components"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode dataset = find(bodyOf(result).path("pages"), "dataset.user-center");
        assertNotNull(dataset, "找不到 user-center 页面树");
        JsonNode api = dataset.path("api");
        assertFalse(api.isMissingNode(), "dataset 必须携带 api 字段");

        if (api.isString()) {
            // 默认构建没有 ping-api-doc/openapi.json：引用保持原样
            assertTrue(api.asString().startsWith("@ResourceReference:"),
                "解析前引用应保持原样，实际 " + api.asString());
        } else {
            // 打包形态：已替换为完整描述
            assertTrue(api.path("method").asString().length() > 0);
            assertTrue(api.path("path").asString().length() > 0);
            assertTrue(api.has("request") && api.has("response"));
        }
    }

    @Test
    void legacyMenusEndpointIsRemoved() throws Exception {
        mockMvc().perform(get("/v1/system/menus"))
            .andExpect(status().isNotFound());
    }

    private JsonNode find(JsonNode node, String id) {
        if (node.isObject()) {
            if (id.equals(node.path("id").asString(""))) {
                return node;
            }
            for (JsonNode child : node.path("containers")) {
                JsonNode found = find(child, id);
                if (found != null) {
                    return found;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                JsonNode found = find(item, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
