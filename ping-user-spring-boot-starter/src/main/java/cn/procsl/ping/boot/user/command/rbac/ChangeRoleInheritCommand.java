package cn.procsl.ping.boot.user.command.rbac;

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
public class ChangeRoleInheritCommand implements Serializable {

    @NotNull Long roleId;

    Long inheritRoleId;
}
