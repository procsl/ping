package cn.procsl.ping.boot.user.rbac;

import java.io.Serializable;
import java.util.Set;

public interface Role extends Serializable {

    String getName();

    Set<Permission> getPermissions();

}
