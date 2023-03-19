package cn.procsl.ping.boot.system.web.rbac;

import cn.procsl.ping.boot.system.domain.rbac.Permission;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class PermissionCreateDTO extends PermissionUpdateDTO {

    @NotNull PermissionType type;

    public Permission convert() {
        return type.factory.apply(operate, resource);
    }
}
