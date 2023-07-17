package cn.procsl.ping.boot.web.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.Link;

@JsonIgnoreProperties(value = {"hreflang", "deprecation", "profile", "templated", "template", "title", "type", "name", "rel"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceLink extends Link {

    private final Link link;

    protected ResourceLink(Link link) {
        this.link = link;
    }

    public static ResourceLink of(Link link) {
        return new ResourceLink(link);
    }

    @Override
    @Schema(example = "/v1/example")
    public String getHref() {
        return link.getHref();
    }

    @Override
    @Schema(example = "application/json")
    public String getMedia() {
        return link.getMedia();
    }


}
