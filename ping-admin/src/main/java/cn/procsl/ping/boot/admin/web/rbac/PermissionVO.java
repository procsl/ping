package cn.procsl.ping.boot.admin.web.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PermissionVO extends PermissionUpdateDTO {

    @Schema
    Long id;

    PermissionType type;

    String summary;

    String description;

}
