package cn.procsl.ping.boot.system.web.rbac;

import cn.procsl.ping.boot.system.domain.rbac.Permission;
import cn.procsl.ping.boot.system.domain.rbac.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class RoleGrantDTO extends RoleNameDTO {

    @Schema(description = "权限列表IDs")
    @NotNull @Size(max = 100) Collection<Long> permissions;

    public RoleGrantDTO(Role role) {
        this.name = role.getName();
        this.permissions = role.getPermissions().stream().map(Permission::getId).collect(Collectors.toSet());
    }

    public RoleGrantDTO(String name, Collection<Long> permissions) {
        super(name);
        this.permissions = permissions;
    }
}
