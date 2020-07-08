package cn.procsl.ping.boot.user.command.rbac;

import cn.procsl.ping.boot.user.domain.rbac.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/07/08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionCommand implements Serializable {

    @NotNull Long roleId;

    @NotNull Permission permission;

}
