package cn.procsl.ping.admin.web.rbac;

import cn.procsl.ping.boot.base.domain.rbac.Permission;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PermissionCreateDTO extends PermissionUpdateDTO {

    @NotNull PermissionType type;

    public Permission convert() {
        return type.factory.apply(option, resource);
    }
}
