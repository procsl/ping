package cn.procsl.ping.admin.web.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleNameDTO implements Serializable {

    @Schema(description = "角色名称, 必须唯一")
    @NotEmpty
    String name;

}
