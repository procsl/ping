package cn.procsl.ping.boot.rest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/03/19
 */
@ConfigurationProperties(prefix = "ping.rest.exception.resolver")
@Getter
@Setter
public class DefaultExceptionResolver {

    private final Map<String, Object> defaultMap = new HashMap<>();
    Map<Class<? extends Throwable>, Map<String, Object>> mappingList = new HashMap<>();

    public DefaultExceptionResolver() {
        mappingList.put(RuntimeException.class, this.defaultMap);
        mappingList.put(Exception.class, this.defaultMap);
        mappingList.put(Throwable.class, this.defaultMap);
    }


}
