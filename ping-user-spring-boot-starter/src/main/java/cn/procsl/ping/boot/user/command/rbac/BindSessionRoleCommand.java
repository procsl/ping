package cn.procsl.ping.boot.user.command.rbac;

import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author procsl
 * @date 2020/07/08
 */
@Value
public class BindSessionRoleCommand implements Serializable {

    @NotNull Long sessionId;

    @NotEmpty Collection<Long> roles;

}
