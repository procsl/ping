package cn.procsl.ping.boot.system.api.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class PermissionVO extends PermissionUpdateDTO {

    @Schema
    Long id;

    PermissionType type;

    public PermissionVO(
            Long id,
            @NotBlank String operate,
            @NotBlank String resource,
            String type) {
        super(operate, resource);
        this.id = id;
        this.type = PermissionType.valueOf(type);
    }
}
