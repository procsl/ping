package cn.procsl.ping.boot.system.api.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleVO extends RoleNameDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long id;

}
