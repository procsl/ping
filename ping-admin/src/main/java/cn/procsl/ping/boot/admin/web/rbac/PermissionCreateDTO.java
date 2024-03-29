package cn.procsl.ping.boot.admin.web.rbac;

import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PermissionCreateDTO extends PermissionUpdateDTO {

    @NotNull PermissionType type;

    public Permission convert() {
        return type.factory.apply(operate, resource);
    }
}
