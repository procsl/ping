package cn.procsl.ping.boot.system.web.rbac;

import cn.procsl.ping.boot.system.domain.rbac.Permission;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionCreateDTO extends PermissionUpdateDTO {

    @NotNull PermissionType type;

    public Permission convert() {
        return type.factory.apply(operate, resource);
    }
}
