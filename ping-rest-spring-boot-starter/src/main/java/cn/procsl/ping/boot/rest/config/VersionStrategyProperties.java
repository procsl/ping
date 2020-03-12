package cn.procsl.ping.boot.rest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author procsl
 * @date 2020/03/08
 */
@ConfigurationProperties(prefix = "ping.rest.version")
@Setter
@Getter
public class VersionStrategyProperties {

    private String pathPrefix = "v[version]";

    private String pathSuffix = "v[version]";

    private String mimeType = "v[version]";

    private String mimeParameter = "v";

    private String header = "X-API-Version=[version]";

    private String query = "version=[version]}";
}
