package cn.procsl.ping.boot.system.domain.ui;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.List;

/**
 * 组件定义
 */
@Getter
@Setter(value = AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UiComponent implements Serializable {


    private String type;

    private String name;

    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String api;

    private List<UiComponent> functions;

    /**
     * 解析后注入
     */
    @JsonIgnore
    private JsonNode apiDefinition;

    @JsonGetter("api")
    public JsonNode api() {
        return apiDefinition;
    }
}
