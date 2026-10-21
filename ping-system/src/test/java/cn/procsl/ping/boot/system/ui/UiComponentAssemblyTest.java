package cn.procsl.ping.boot.system.ui;

import cn.procsl.ping.boot.system.TestSystemApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 组件聚合端点状态验证：{@code GET /v1/ui/components} 已由 {@code redesign-ping-ui-framework}
 * 变更恢复（新架构：功能树 + openapi 内联，{@code UIAutoConfiguration} 启动期编译一次并注册端点），
 * 应返回 200；旧 {@code GET /v1/system/menus} 已下线，仍返回 404。
 *
 * <p>{@code authenticates-prefix} 采用与 {@code ping-distribute/application.properties} 一致的值：
 * 认证策略不在本变更范围内，这里只保证被测接口在与生产一致的前缀配置下可达。
 */
@SpringBootTest(
    classes = TestSystemApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = "procsl.ping.boot.system.authenticates-prefix=/v1/system")
class UiComponentAssemblyTest {

    @Autowired
    WebApplicationContext context;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void componentsEndpointIsRestored() throws Exception {
        mockMvc().perform(get("/v1/ui/components"))
            .andExpect(status().isOk());
    }

    @Test
    void legacyMenusEndpointIsRemoved() throws Exception {
        mockMvc().perform(get("/v1/system/menus"))
            .andExpect(status().isNotFound());
    }
}
