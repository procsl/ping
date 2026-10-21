package cn.procsl.ping.boot.ui.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端点测试（任务 3.1）：{@code GET /v1/ui/components} 返回 200 + 合法 JSON（编译产物形态）。
 *
 * <p>standalone MockMvc 不加载全局登录拦截器（鉴权语义由集成验证覆盖）；
 * controller 无状态：构造时经 factory 验证输入可编译，请求时现场编译（测试资源即输入）。
 */
class UIComponentsControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 无参构造：构造过程即启动验证（输入缺失/断链则此处即失败）
        mockMvc = MockMvcBuilders.standaloneSetup(new UIComponentsController()).build();
    }

    @Test
    void returns200WithAppRootTree() throws Exception {
        mockMvc.perform(get("/v1/ui/components"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("app"))
            .andExpect(jsonPath("$.id").value("ping.app"))
            .andExpect(jsonPath("$.children[0].type").value("page"))
            .andExpect(jsonPath("$.children[0].id").value("user"));
    }

    @Test
    void functionNodeSerializedAsSelfContained() throws Exception {
        mockMvc.perform(get("/v1/ui/components"))
            .andExpect(status().isOk())
            // NON_EMPTY：function 无 children 键、page 无 properties 键
            .andExpect(jsonPath("$.children[0].children[0].children").doesNotExist())
            .andExpect(jsonPath("$.children[0].properties").doesNotExist())
            // 内联产物：operationId/method/path/route 齐备，2xx responses
            .andExpect(jsonPath("$.children[0].children[0].properties.operationId").value("findUsers"))
            .andExpect(jsonPath("$.children[0].children[0].properties.method").value("GET"))
            .andExpect(jsonPath("$.children[0].children[0].properties.path").value("/v1/system/users"))
            .andExpect(jsonPath("$.children[0].children[0].properties.route").value("user/list"))
            .andExpect(jsonPath("$.children[0].children[0].properties.responses.200").exists())
            .andExpect(jsonPath("$.children[0].children[0].properties.responses.400").doesNotExist())
            // $ref 全展开：schema 本体在，无 $ref 键
            .andExpect(jsonPath(
                "$.children[0].children[0].properties.responses.200.content['application/json'].schema.type")
                .value("object"));
    }

    @Test
    void endpointCompilesOnEachRequest() throws Exception {
        // 无状态语义：每次请求现场编译，连续两次返回等价 JSON（不同实例）
        String first = mockMvc.perform(get("/v1/ui/components"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String second = mockMvc.perform(get("/v1/ui/components"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertFalse(first.isBlank());
        assertEquals(first, second);
    }
}
