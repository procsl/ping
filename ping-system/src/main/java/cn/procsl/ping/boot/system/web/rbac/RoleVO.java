package cn.procsl.ping.boot.system.web.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleVO extends RoleNameDTO {

    @Schema(required = true)
    Long id;

}
