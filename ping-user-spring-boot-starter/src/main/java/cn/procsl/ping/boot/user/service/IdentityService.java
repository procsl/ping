package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.domain.rbac.entity.Identity;
import cn.procsl.ping.boot.user.domain.rbac.entity.IdentityId;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import cn.procsl.ping.boot.user.domain.rbac.service.RoleConstraintService;
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
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.EMPTY_SET;

/**
 * 身份服务
 *
 * @author procsl
 * @date 2020/06/24
 */
@Named
@Singleton
@RequiredArgsConstructor
@Validated
@Transactional(rollbackFor = Exception.class)
public class IdentityService {

    @Inject
    final private JpaRepository<Identity, Long> identityJpaRepository;

    @Inject
    final private JpaRepository<Role, Long> roleJpaRepository;

    @Inject
    final private RoleConstraintService roleConstraintService;

    /**
     * 创建身份,初始的身份不会关联任何有效的角色
     *
     * @return 返回创建成功后的身份标识
     * @throws BusinessException 创建失败,则返回业务异常
     */
    public IdentityId create() throws BusinessException {
        Identity identity = Identity
                .creator()
                .active(true)
                .done();
        identityJpaRepository.save(identity);
        return new IdentityId(identity.getId());
    }

    /**
     * 删除指定的身份, 同时将删除与角色之间的关联关系, 不会删除角色
     *
     * @param identityId 身份ID
     */
    public void delete(@NotNull IdentityId identityId) {
        identityJpaRepository.deleteById(identityId.getId());
    }

    /**
     * 禁用 指定的身份
     *
     * @param identityId 身份ID
     * @throws BusinessException 业务异常
     */
    public void disable(@NotNull IdentityId identityId) throws BusinessException {
        Optional<Identity> optionalIdentity = this.identityJpaRepository.findById(identityId.getId());
        optionalIdentity.ifPresent(identity -> {
            identity.disable();
            this.identityJpaRepository.save(identity);
        });
    }

    /**
     * 启用指定的身份
     *
     * @param identityId 身份ID
     * @throws BusinessException 业务异常
     */
    public void enable(@NotNull IdentityId identityId) throws BusinessException {
        Optional<Identity> optionalIdentity = this.identityJpaRepository.findById(identityId.getId());
        optionalIdentity.ifPresent(identity -> {
            identity.enable();
            this.identityJpaRepository.save(identity);
        });
    }

    /**
     * 为指定的身份绑定角色
     *
     * @param identityId 身份ID
     * @param roleIds    角色IDs
     * @throws BusinessException 如果绑定的角色错误或者不存在, 抛出此异常
     */
    public void bindRoles(@NotNull IdentityId identityId, @NotEmpty Set<Long> roleIds) throws BusinessException {
        Identity identity = this.identityJpaRepository
                .findById(identityId.getId())
                .orElseThrow(() -> new BusinessException("身份不存在", identityId));

        for (Long roleId : roleIds) {
            Role role = this.roleJpaRepository
                    .findById(roleId).orElseThrow(() -> new BusinessException("角色不存在", roleId));

            Integer max = roleConstraintService.checkMaxCondition(identity, role);
            if (max > 0) {
                throw new BusinessException("角色数量限制", roleId, max);
            }
            Set<Long> requires = roleConstraintService.checkRequireCondition(identity, role);
            if (!requires.isEmpty()) {
                throw new BusinessException("角色前置条件限制", roleId, requires);
            }
            Set<Long> excludes = roleConstraintService.checkExcludeCondition(identity, role);
            if (!excludes.isEmpty()) {
                throw new BusinessException("互斥角色限制", roleId, excludes);
            }
            identity.addRole(roleId);
        }
        this.identityJpaRepository.save(identity);
    }

    /**
     * 移除指定身份的角色
     *
     * @param identityId 身份ID
     * @param roleId     角色ID
     * @throws BusinessException 如果移除失败, 则抛出此异常
     */
    public void unbindRole(@NotNull IdentityId identityId, @NotNull Long roleId) throws BusinessException {
        Identity identity = this.identityJpaRepository
                .findById(identityId.getId())
                .orElseThrow(() -> new BusinessException("身份不存在", identityId));
        identity.remove(roleId);
        this.identityJpaRepository.save(identity);
    }


    /**
     * 获取指定身份的所有角色
     *
     * @param identityId
     * @return
     */
    @Transactional(readOnly = true)
    public Set<Long> getRoles(@NotNull IdentityId identityId) {
        Identity identity = this.identityJpaRepository
                .findById(identityId.getId())
                .orElseThrow(() -> new BusinessException("身份不存在", identityId));
        if (identity.getRoles() == null) {
            return EMPTY_SET;
        }
        return Collections.unmodifiableSet(identity.getRoles());
    }

    @Transactional(readOnly = true)
    public Identity findById(@NotNull IdentityId identityId) {
        return identityJpaRepository.findById(identityId.getId()).orElse(null);
    }
}
