package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.user.domain.rbac.entity.Identity;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.unmodifiableSet;

/**
 * rbac 角色约束服务
 *
 * @author procsl
 * @date 2020/06/24
 */
@Named
@Singleton
public class RoleConstraintService {

    /**
     * 检查最大约束数量
     *
     * @param identity 指定的身份
     * @param role     指定的角色
     * @return 返回当前身份与当前角色约束数量的差值, 如果为0 则代表校验通过
     */
    public int checkMaxCondition(Identity identity, Role role) {
        Integer max = role.getMax();
        if (max == null || max == 0) {
            return 0;
        }

        Set<Long> roles = identity.getRoles();
        if (roles == null) {
            return 0;
        }

        if (roles.size() < max) {
            return 0;
        }
        return max;
    }

    /**
     * 校验前置角色约束
     *
     * @param identity 指定的身份
     * @param role     指定的角色
     * @return 如果通过约束, 则返回empty set, 否则返回缺少的前置角色IDs
     */
    public Set<Long> checkRequireCondition(Identity identity, Role role) {
        if (role.getRequires() == null || role.getRequires().isEmpty()) {
            return EMPTY_SET;
        }

        Set<Long> roles = identity.getRoles();
        if (roles == null || roles.isEmpty()) {
            return unmodifiableSet(role.getRequires());
        }

        Set<Long> requires = new HashSet(role.getRequires());
        requires.removeAll(roles);
        return requires;
    }

    /**
     * 检查互斥角色
     *
     * @param identity 指定的身份
     * @param role     指定的角色
     * @return 返回冲突的互斥角色
     */
    public Set<Long> checkExcludeCondition(Identity identity, Role role) {
        Set<Long> excludes = role.getRequires();
        if (excludes == null || excludes.isEmpty()) {
            return EMPTY_SET;
        }

        Set<Long> roles = identity.getRoles();
        if (roles == null || roles.isEmpty()) {
            return EMPTY_SET;
        }
        return roles.stream().filter(item -> excludes.contains(item)).collect(Collectors.toSet());
    }
}

