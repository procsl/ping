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
 * 组件聚合链路下线验证：
 * 构建期聚合 + 启动期装配的 {@code GET /v1/ui/components} 已随 {@code 4fb05681} 移除
 * （恢复不在 add-ping-ui-web-sdk 变更范围内），旧 {@code GET /v1/system/menus} 亦已下线。
 * 两个接口当前均应返回 404。
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
    void componentsEndpointIsRemoved() throws Exception {
        mockMvc().perform(get("/v1/ui/components"))
            .andExpect(status().isNotFound());
    }

    @Test
    void legacyMenusEndpointIsRemoved() throws Exception {
        mockMvc().perform(get("/v1/system/menus"))
            .andExpect(status().isNotFound());
    }
}
