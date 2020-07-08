package cn.procsl.ping.boot.user.service.rbac;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.command.rbac.BindSessionRoleCommand;
import cn.procsl.ping.boot.user.command.rbac.UnbindSessionRoleCommand;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import cn.procsl.ping.boot.user.domain.rbac.entity.Session;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Valid;
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
@Slf4j
public class SessionService {

    @Inject
    final private JpaRepository<Session, Long> sessionJpaRepository;

    @Inject
    final private JpaRepository<Role, Long> roleJpaRepository;

    /**
     * 创建session
     *
     * @return 返回创建成功后的session标识
     * @throws BusinessException 创建失败,则返回业务异常
     */
    public Long create() throws BusinessException {
        Session session = new Session(true);
        sessionJpaRepository.save(session);
        return session.getId();
    }

    /**
     * 删除指定的session, 同时将删除与角色之间的关联关系, 不会删除角色
     *
     * @param sessionId sessionID
     */
    public void delete(@NotNull Long sessionId) {
        log.info("删除{}", sessionId);
        sessionJpaRepository.deleteById(sessionId);
    }

    /**
     * 禁用 指定的session
     *
     * @param sessionId sessionID
     * @throws BusinessException 业务异常
     */
    public void disable(@NotNull Long sessionId) throws BusinessException {
        Session session = load(sessionId);
        session.disable();
        this.sessionJpaRepository.save(session);
        log.info("{}被禁用", sessionId);
    }

    /**
     * 启用指定的session
     *
     * @param sessionId sessionID
     * @throws BusinessException 业务异常
     */
    public void enable(@NotNull Long sessionId) throws BusinessException {
        Session session = load(sessionId);
        session.enable();
        this.sessionJpaRepository.save(session);
        log.info("{}被启用", sessionId);
    }

    /**
     * 为指定的session绑定角色
     *
     * @param command 角色绑定
     * @throws BusinessException 如果绑定的角色错误或者不存在, 抛出此异常
     */
    public void bindRoles(@NonNull @Valid BindSessionRoleCommand command) throws BusinessException {
        Session session = load(command.getSessionId());
        for (Long roleId : command.getRoles()) {
            boolean exists = this.roleJpaRepository.existsById(roleId);
            if (!exists) {
                throw new BusinessException("角色不存在", roleId);
            }
            session.addRole(roleId);
        }
        log.info("绑定[{}]角色", command.getSessionId());
        log.debug("角色[{}]已绑定[{}]", command.getSessionId(), command.getRoles());
        this.sessionJpaRepository.save(session);
    }

    /**
     * 移除指定session的角色
     *
     * @param command 解绑角色
     * @throws BusinessException 如果移除失败, 则抛出此异常
     */
    public void unbindRole(@NonNull @Valid UnbindSessionRoleCommand command) throws BusinessException {
        Session session = load(command.getSessionId());
        session.remove(command.getRoleId());
        this.sessionJpaRepository.save(session);
        log.info("解除[{}]绑定的角色[{}]", command.getSessionId(), command.getRoleId());
    }


    /**
     * 获取指定session的所有角色
     *
     * @param sessionId
     * @return
     */
    @Transactional(readOnly = true)
    public Set<Long> getRoles(@NotNull Long sessionId) {
        Session session = load(sessionId);

        if (!session.isActive()) {
            return EMPTY_SET;
        }

        if (session.getRoles() == null) {
            return EMPTY_SET;
        }
        return Collections.unmodifiableSet(session.getRoles());
    }

    Session load(@NotNull Long sessionId) {
        return this.sessionJpaRepository
                .findById(sessionId)
                .orElseThrow(() -> new BusinessException("session不存在", sessionId));
    }
}
