package cn.procsl.ping.app.config;

import io.socket.engineio.server.EngineIoServerOptions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

@Slf4j
@Configuration
public class WebServerSSLConfigure implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {


    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        ThreadFactory threadFactory = Thread.ofVirtual().name("virtual-", 0).factory();
        factory.addProtocolHandlerCustomizers(protocolHandler -> protocolHandler.setExecutor(
                Executors.newThreadPerTaskExecutor(new ThreadFactoryWrapper(threadFactory))));
    }

    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {
        ThreadFactory threadFactory = Thread.ofVirtual().name("async-task-", 0).factory();
        ExecutorService execute = Executors.newThreadPerTaskExecutor(new ThreadFactoryWrapper(threadFactory));
        return new TaskExecutorAdapter(execute);
    }

    @Bean
    public EngineIoServerOptions engineIoServerOptions() {
        EngineIoServerOptions options = EngineIoServerOptions.newFromDefault();
        ThreadFactory threadFactory = Thread.ofVirtual().name("socket.io-", 0).factory();
        ScheduledExecutorService execute = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryWrapper(threadFactory));
        options.setScheduledExecutorService(execute);
        return options;
    }

    static final class ThreadFactoryWrapper implements ThreadFactory {

        final ThreadFactory factory;

        public ThreadFactoryWrapper(ThreadFactory factory) {
            this.factory = factory;
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return factory.newThread(() -> {
//                String requestId = UUID.randomUUID().toString().replaceAll("-", "");
//                MDC.put("RequestId", requestId);
                r.run();
//                MDC.clear();
            });
        }

    }


}
