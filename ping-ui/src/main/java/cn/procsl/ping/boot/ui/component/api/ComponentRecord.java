package cn.procsl.ping.boot.ui.component.api;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.web.ProjectedPayload;

import java.util.Collection;

@ProjectedPayload
public interface ComponentRecord {


    @Schema(description = "组件ID")
    @SecurityId(scope = "component") Long getId();

    String getName();

    Collection<String> getTags();

}
