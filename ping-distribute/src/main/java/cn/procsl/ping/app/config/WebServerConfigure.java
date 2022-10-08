package cn.procsl.ping.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class WebServerConfigure implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {


    @Override
    public void customize(TomcatServletWebServerFactory factory) {
//        ThreadFactory threadFactory = Thread.ofVirtual().name("virtual-", 0).factory();
//        factory.addProtocolHandlerCustomizers(protocolHandler -> protocolHandler.setExecutor(
//                Executors.newThreadPerTaskExecutor(new ThreadFactoryWrapper(threadFactory))));
    }


}
