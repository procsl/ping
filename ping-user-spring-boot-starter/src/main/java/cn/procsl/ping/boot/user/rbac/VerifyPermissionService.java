package cn.procsl.ping.boot.user.rbac;

import java.util.Collection;

/**
 * 权限校验服务
 */
public interface VerifyPermissionService {

    /**
     * 校验权限是否合法
     *
     * @param permissions 权限列表
     * @throws RbacException 如果不合法则抛出异常
     */
    void verify(Collection<String> permissions) throws RbacException;
}
