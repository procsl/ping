package cn.procsl.ping.boot.user.command.rbac;

import lombok.Value;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/07/08
 */
@Value
public class UnbindSessionRoleCommand implements Serializable {

    @NotNull Long sessionId;

    @NotNull Long roleId;
}
