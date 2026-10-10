package cn.procsl.ping.boot.ui.component.api;

import cn.procsl.ping.boot.ui.TestUiApplication;
import cn.procsl.ping.boot.ui.component.UIComponentRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 组件描述接口契约（spec：组件描述接口返回内存注册表中的组件）。
 */
@SpringBootTest(
    classes = TestUiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ComponentControllerTest {

    static final String PATH = "/v1/ui/components";

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Autowired
    WebApplicationContext context;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    private MvcResult call() throws Exception {
        return mockMvc().perform(get(PATH)).andExpect(status().isOk()).andReturn();
    }

    @Test
    void nonEmptyRegistryReturnsTypesAndPagesAsJson() throws Exception {
        MvcResult result = mockMvc().perform(get(PATH))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();

        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertTrue(body.path("types").isArray(), "响应必须含 types 数组");
        assertTrue(body.path("pages").isArray(), "响应必须含 pages 数组");
        assertTrue(body.path("types").size() >= 16,
            "非空注册表应下发至少 16 个常用组件类型，实际 " + body.path("types").size());
    }

    @Test
    void repeatedRequestsReturnIdenticalBody() throws Exception {
        String first = call().getResponse().getContentAsString();
        String second = call().getResponse().getContentAsString();
        assertEquals(first, second, "连续两次请求响应必须一致");
    }

    @Test
    void emptyRegistryReturnsTwoEmptyArrays() throws Exception {
        UIComponentRegistry empty = new UIComponentRegistry(null);
        MockMvc standalone = MockMvcBuilders
            .standaloneSetup(new ComponentController(empty))
            .build();

        standalone.perform(get(PATH))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"types\":[],\"pages\":[]}"));
    }
}
