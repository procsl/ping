package cn.procsl.ping.boot.admin.auth.access;

import cn.procsl.ping.boot.admin.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GrantedAuthorityFactory {

    private final static Map<Class<? extends Permission>, Function<Permission, GrantedAuthority>> grantedAuthorityMap = new HashMap<>();

    static {
        grantedAuthorityMap.put(HttpPermission.class, item -> new HttpGrantedAuthority((HttpPermission) item));
    }

    public static GrantedAuthority create(Permission permission) {
        Function<Permission, GrantedAuthority> factor = grantedAuthorityMap.get(permission.getClass());
        if (factor != null) {
            return factor.apply(permission);
        }
        return null;
    }

}
