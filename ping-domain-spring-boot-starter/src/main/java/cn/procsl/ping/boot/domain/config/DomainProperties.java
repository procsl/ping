package cn.procsl.ping.boot.domain.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author procsl
 * @date 2020/04/06
 */

@ConfigurationProperties(prefix = "ping.domain")
@Getter
@Setter
public class DomainProperties {

    private String foreignKeyNamePrefix = "";

    private String uniqueKeyPrefix = "";

    private String indexNamePrefix = "";

    private String tablePrefix = "p";

    private Map<String, String> tableNamePrefix;

    private String dot = "_";

}
