package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import cn.procsl.ping.boot.user.domain.rbac.entity.Session;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * 角色关系解释器
 *
 * @author procsl
 * @date 2020/07/06
 */
@Named
@Singleton
public class RelationInterpreter {

    public boolean interpreter(Session session, Role role) {
        return true;
    }
}
