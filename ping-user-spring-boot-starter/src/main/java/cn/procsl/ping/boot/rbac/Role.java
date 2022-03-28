package cn.procsl.ping.boot.rbac;

import java.io.Serializable;
import java.util.Set;

public interface Role extends Serializable {

    String getName();

    Set<Permission> getPermissions();

}
