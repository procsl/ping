package cn.procsl.ping.boot.system.api.menu;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class MenuDTO implements Serializable {

    @SecurityId(scope = "menu")
    @Schema(description = "父节点ID, 如果为空则代表创建根节点", nullable = true, implementation = String.class, example = "Q0GgEiBbxEB4kCNHheTNb", minLength = 22, maxLength = 22)
    Long parentId;

    @Schema(description = "菜单名称", example = "用户管理")
    @NotNull @Size(max = 40) String name;

    @Schema(description = "路由URL", example = "/console/user-center.html")
    @Size(max = 40)
    String router;

}
