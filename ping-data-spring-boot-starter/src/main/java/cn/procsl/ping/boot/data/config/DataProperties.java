package cn.procsl.ping.boot.data.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author procsl
 * @date 2020/04/06
 */

@ConfigurationProperties(prefix = "ping.data")
@Getter
@Setter
public class DataProperties {

    private String foreignKeyNamePrefix = "fk";

    private String uniqueKeyPrefix = "uk";

    private String indexNamePrefix = "in";

    protected String tablePrefix ="pb";

    protected String dot = "_";

}