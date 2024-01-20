package cn.procsl.ping.boot.web.component;

import cn.procsl.ping.boot.common.utils.TraceIdGenerator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import static java.util.concurrent.Executors.newThreadPerTaskExecutor;

public class TomcatProtocolHandlerConfig {


    @Bean
    public TomcatProtocolHandlerCustomizer<?> tomcatProtocolHandlerCustomizer() {
        return protocolHandler -> {
            ExecutorService task = newThreadPerTaskExecutor(new InnerThreadFactory());
            protocolHandler.setExecutor(task);
        };
    }

    @Slf4j
    static class InnerThreadFactory implements ThreadFactory {

        final ThreadFactory factory = Thread.ofVirtual().factory();

        final TraceIdGenerator generator = TraceIdGenerator.initTraceId("yyyyMMdd", 16);

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return factory.newThread(() -> {
                Thread.currentThread().setName(generator.generateId());
                runnable.run();
                MDC.clear();
            });
        }
    }

}
