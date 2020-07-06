package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import cn.procsl.ping.boot.user.domain.rbac.entity.Session;
import cn.procsl.ping.boot.user.domain.rbac.entity.SessionId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

import static java.util.Collections.EMPTY_SET;

/**
 * session服务
 *
 * @author procsl
 * @date 2020/06/24
 */
@Named
@Singleton
@RequiredArgsConstructor
@Validated
@Transactional(rollbackFor = Exception.class)
public class SessionService {

    @Inject
    final private JpaRepository<Session, Long> sessionJpaRepository;

    @Inject
    final private JpaRepository<Role, Long> roleJpaRepository;

    /**
     * 创建session,初始的session不会关联任何有效的角色
     *
     * @return 返回创建成功后的session标识
     * @throws BusinessException 创建失败,则返回业务异常
     */
    public SessionId create() throws BusinessException {
        Session session = Session
                .creator()
                .active(true)
                .done();
        sessionJpaRepository.save(session);
        return new SessionId(session.getId());
    }

    /**
     * 删除指定的session, 同时将删除与角色之间的关联关系, 不会删除角色
     *
     * @param sessionId sessionID
     */
    public void delete(@NotNull SessionId sessionId) {
        sessionJpaRepository.deleteById(sessionId.getId());
    }

    /**
     * 禁用 指定的session
     *
     * @param sessionId sessionID
     * @throws BusinessException 业务异常
     */
    public void disable(@NotNull SessionId sessionId) throws BusinessException {
        this.sessionJpaRepository.findById(sessionId.getId())
                .ifPresent(session -> {
                    session.disable();
                    this.sessionJpaRepository.save(session);
                });
    }

    /**
     * 启用指定的session
     *
     * @param sessionId sessionID
     * @throws BusinessException 业务异常
     */
    public void enable(@NotNull SessionId sessionId) throws BusinessException {
        this.sessionJpaRepository.findById(sessionId.getId())
                .ifPresent(session -> {
                    session.enable();
                    this.sessionJpaRepository.save(session);
                });
    }

    /**
     * 为指定的session绑定角色
     *
     * @param sessionId sessionID
     * @param roleIds   角色IDs
     * @throws BusinessException 如果绑定的角色错误或者不存在, 抛出此异常
     */
    public void bindRoles(@NotNull SessionId sessionId, @NotEmpty Set<Long> roleIds) throws BusinessException {
        Session session = this.sessionJpaRepository
                .findById(sessionId.getId())
                .orElseThrow(() -> new BusinessException("session不存在", sessionId));

        for (Long roleId : roleIds) {
            boolean exists = this.roleJpaRepository.existsById(roleId);
            if (!exists) {
                throw new BusinessException("角色不存在", roleId);
            }
            session.addRole(roleId);
        }
        this.sessionJpaRepository.save(session);
    }

    /**
     * 移除指定session的角色
     *
     * @param sessionId sessionID
     * @param roleId    角色ID
     * @throws BusinessException 如果移除失败, 则抛出此异常
     */
    public void unbindRole(@NotNull SessionId sessionId, @NotNull Long roleId) throws BusinessException {
        Session session = this.sessionJpaRepository
                .findById(sessionId.getId())
                .orElseThrow(() -> new BusinessException("session不存在", sessionId));
        session.remove(roleId);
        this.sessionJpaRepository.save(session);
    }


    /**
     * 获取指定session的所有角色
     *
     * @param sessionId
     * @return
     */
    @Transactional(readOnly = true)
    public Set<Long> getRoles(@NotNull SessionId sessionId) {
        Session session = this.sessionJpaRepository
                .findById(sessionId.getId())
                .orElseThrow(() -> new BusinessException("session不存在", sessionId));
        if (session.getRoles() == null) {
            return EMPTY_SET;
        }
        return Collections.unmodifiableSet(session.getRoles());
    }

    @Transactional(readOnly = true)
    public Session findById(@NotNull SessionId sessionId) {
        return sessionJpaRepository.findById(sessionId.getId()).orElse(null);
    }
}
