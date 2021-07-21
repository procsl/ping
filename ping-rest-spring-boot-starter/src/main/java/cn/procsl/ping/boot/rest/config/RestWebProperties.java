package cn.procsl.ping.boot.rest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Set;

/**
 * @author procsl
 * @date 2020/02/18
 */
@ConfigurationProperties(prefix = "ping.rest.web")
@Getter
@Setter
public class RestWebProperties {

    public final static String modelKey = RestWebProperties.class.getName() + "Key";
    private boolean wrapper = true;
    private String rootName = "root";
    private boolean writeXmlDeclaration = true;
    private boolean indentOutput = true;
    /**
     * 支持的元媒体类型
     */
    Set<MetaMediaType> metaMediaTypes = Collections.singleton(MetaMediaType.json);

    /**
     * 是否启用自定义媒体类型
     */
    RepresentationStrategy representationStrategy = RepresentationStrategy.system_mime;

    /**
     * 如果不为null 则生成 诸如 application/vnd.api+json 的格式
     * <p>
     * 自定义媒体类型
     */
    String mimeSubtype = "vnd.api";

    /**
     * 是否启用版本管理
     */
    boolean enableVersion = true;

    /**
     * 表征媒体类型
     */
    public enum MetaMediaType {
        json, xml, yaml
    }

    /**
     * 表征策略
     */
    public enum RepresentationStrategy {

        // application/vnd.api+json
        custom_mime,

        // application/json
        system_mime
    }


}
