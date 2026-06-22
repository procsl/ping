package cn.procsl.ping.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(DistributeConfiguration.RequestProxyHint.class)
public class DistributeConfiguration {


    // 这样，以后所有跟 GraalVM 相关的特殊补丁规则，都可以统一写在这个内部类里
    static class RequestProxyHint implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 注册全局 HttpServletRequest 动态代理
            hints.proxies().registerJdkProxy(HttpServletRequest.class);
            hints.proxies().registerJdkProxy(HttpServletResponse.class);
        }
    }

}
