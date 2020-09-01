package cn.procsl.ping.boot.domain.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author procsl
 * @date 2020/04/06
 */

@ConfigurationProperties(prefix = "ping.domain")
@Getter
@Setter
public class DomainProperties {

    private String foreignKeyNamePrefix = "fk";

    private String uniqueKeyPrefix = "uk";

    private String indexNamePrefix = "in";

    private String tablePrefix = "pb";

    private String dot = "_";

}
