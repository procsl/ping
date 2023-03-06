package cn.procsl.ping.boot.connect;

import cn.procsl.ping.boot.connect.server.Namespace;
import cn.procsl.ping.boot.connect.server.SocketIOServer;
import cn.procsl.ping.boot.connect.server.SocketIOServerBuilder;
import io.socket.engineio.server.EngineIoServerOptions;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Map;

@EnableWebSocket
@AutoConfiguration
public class ConnectAutoConfiguration implements WebSocketConfigurer, WebMvcConfigurer {

    private final ApplicationContext context;

    public ConnectAutoConfiguration(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        SocketIOServer socketIOServer = context.getBean(SocketIOServer.class);
        registry.addHandler(socketIOServer, SocketIOServer.endpoint).addInterceptors(socketIOServer);
    }

    @Bean
    @ConditionalOnMissingBean
    public SocketIOServer socketIOServer(@Autowired(required = false) EngineIoServerOptions options) {
        SocketIOServerBuilder builder = SocketIOServerBuilder.builder();
        if (options != null) {
            builder.options(options);
        }
        try {
            Map<String, Object> beans = this.context.getBeansWithAnnotation(Namespace.class);
            beans.forEach((k, v) -> builder.addHandlers(v));
        } catch (BeansException ignored) {
        }
        return new SocketIOServer(builder.build());
    }

    @Override
    @SneakyThrows
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/socket/**").addResourceLocations("classpath:/META-INF/resources/socketio/")
                .setCacheControl(CacheControl.noCache()).resourceChain(true);
    }


}
