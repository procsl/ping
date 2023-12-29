package cn.procsl.ping.boot.system.api.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleNameDTO implements Serializable {

    @Schema(description = "角色名称, 必须唯一")
    @NotEmpty(groups = NotEmpty.class)
    String name;

}
