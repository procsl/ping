package cn.procsl.ping.boot.system.api.menu;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
public class MenuVO implements Serializable {

    @SecurityId
    Long id;

    String name;

    @SecurityId
    Long parentId;

    @Schema(description = "路由URL")
    String router;

    Integer depth;
}