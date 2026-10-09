package cn.procsl.ping.boot.ui;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 静态产物落位验证：主壳、本模块薄壳与渲染器 bundle 打进 classpath 后可直接访问。
 * 仅起 Web 层（不加载组件扫描），避免被存量未完成的机制代码阻断。
 */
@SpringBootTest(classes = StaticAssetsTest.WebOnlyConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StaticAssetsTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class WebOnlyConfig {
    }

    @LocalServerPort
    int port;

    private RestClient client() {
        return RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    private HttpStatusCode get(String path) {
        return client().get().uri(path).retrieve().toBodilessEntity().getStatusCode();
    }

    private String body(String path) {
        return client().get().uri(path).retrieve().body(String.class);
    }

    @Test
    void mainShellLoadsRenderer() {
        assertEquals(200, get("/index.html").value());
        String html = body("/index.html");
        assertTrue(html.contains("assets/renderer/renderer.js"), "主壳应引用渲染器 bundle");
        assertTrue(html.contains("mount("), "主壳应调用渲染器 mount 入口");
    }

    @Test
    void moduleEntryUsesNamespace() {
        assertEquals(200, get("/ui/index.html").value());
        String html = body("/ui/index.html");
        assertTrue(html.contains("namespace: \"ui\""), "薄壳命名空间应替换为模块标识");
        assertTrue(!html.contains("@ping.module@"), "薄壳占位符必须已被替换");
    }

    @Test
    void rendererBundleReachableAfterPackage() {
        // renderer.js 由 prepare-package 阶段复制，clean test 场景下跳过（打包链路由 4.9 手工验证覆盖）
        if (!new ClassPathResource("META-INF/resources/assets/renderer/renderer.js").exists()) {
            return;
        }
        assertEquals(200, get("/assets/renderer/renderer.js").value());
        assertEquals(200, get("/assets/renderer/renderer.css").value());
    }

    @Test
    void entryTemplatePublishedUnfiltered() {
        ClassPathResource template = new ClassPathResource("META-INF/ping/entry/index.html");
        assertNotNull(template);
        assertTrue(template.exists(), "共享薄壳模板必须随 jar 发布");
        try (var is = template.getInputStream()) {
            String content = new String(is.readAllBytes());
            assertTrue(content.contains("@ping.module@"), "模板占位符供业务模块打包期替换");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
