package cn.procsl.ping.boot.rest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static java.util.Collections.EMPTY_LIST;

/**
 * @author procsl
 * @date 2020/02/18
 */
@ConfigurationProperties(prefix = "ping.rest.web")
@Getter
@Setter
public class RestWebProperties {

    private String contentNegotiationParameterName = "content";

    private boolean wrapper = true;

    private String rootName = "root";

    private boolean writeXmlDeclaration = true;

    private boolean indentOutput = true;

    private String modelKey = "modelKey";

    private String filterParameterName = "filter";

    private String filterTypeParameterName = "pattern";

    private List<String> restControllerPackageName = EMPTY_LIST;

    private String mimeSubtype = "vnd.api";

    private boolean enableVersion = true;
}
