package cn.procsl.ping.app.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(DistributeConfiguration.RequestProxyHint.class)
public class DistributeConfiguration {


//    @Bean
//    public ModelResolver modelResolver() throws Exception {
//        JsonMapper map = new JsonMapper();
//        map.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
//        return new ModelResolver(map);
//    }

//    @Bean
//    @ConditionalOnProperty(name = "spring.jackson.property-naming-strategy", havingValue = "SNAKE_CASE", matchIfMissing = true)
//    @ConditionalOnClass(name = {"io.swagger.v3.core.jackson.ModelResolver",
//        "com.fasterxml.jackson.databind.ObjectMapper",
//        "org.springdoc.core.properties.SpringDocConfigProperties"})
//    @ConditionalOnBean(type = "org.springdoc.core.properties.SpringDocConfigProperties")
    public Object modelResolver(ApplicationContext context) throws Exception {
//        log.info("加载解析: spring.jackson.property-naming-strategy Springdoc配置项");
        // 2. 动态加载 SpringDocConfigProperties 实例
        Class<?> propertiesClass = Class.forName("org.springdoc.core.properties.SpringDocConfigProperties");
        Object properties = context.getBean(propertiesClass);
        boolean isOpenapi31 = (boolean) propertiesClass.getMethod("isOpenapi31").invoke(properties);

        // 3. 动态创建 Jackson 2 的 ObjectMapper （完全由字符串驱动，躲过任何编译器的眼睛）
        Class<?> objectMapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
        Object mapper = objectMapperClass.getConstructor().newInstance();

        // 4. 动态获取 Jackson 2 的 SNAKE_CASE 策略
        Class<?> namingStrategiesClass = Class.forName("com.fasterxml.jackson.databind.PropertyNamingStrategies");
        Object snakeCaseStrategy = namingStrategiesClass.getField("SNAKE_CASE").get(null);

        // 5. 将策略注入到 Jackson 2 的 ObjectMapper 中
        Class<?> strategyClass = Class.forName("com.fasterxml.jackson.databind.PropertyNamingStrategy");
        objectMapperClass.getMethod("setPropertyNamingStrategy", strategyClass).invoke(mapper, snakeCaseStrategy);

        // 6. 组装并返回 ModelResolver
        Class<?> resolverClass = Class.forName("io.swagger.v3.core.jackson.ModelResolver");
        Object resolver = resolverClass.getConstructor(objectMapperClass).newInstance(mapper);

        return resolverClass.getMethod("openapi31", boolean.class).invoke(resolver, isOpenapi31);
    }

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
