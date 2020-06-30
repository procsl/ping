package cn.procsl.ping.boot.user.facade;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.domain.rbac.entity.Identity;
import cn.procsl.ping.boot.user.domain.rbac.entity.IdentityId;
import cn.procsl.ping.boot.user.domain.rbac.service.IdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;

/**
 * 身份服务
 *
 * @author procsl
 * @date 2020/06/24
 */
@Named
@Singleton
@RequiredArgsConstructor
public class IdentityFacade {

    @Inject
    final IdentityService identityService;

    final JpaRepository<Identity, Long> jpaRepository;

    /**
     * 创建身份,初始的身份不会关联任何有效的角色
     *
     * @return 返回创建成功后的身份标识
     * @throws BusinessException 创建失败,则返回业务异常
     */
    @Transactional(rollbackOn = Exception.class)
    public IdentityId create() throws BusinessException {
        return identityService.create();
    }

    /**
     * 删除指定的身份, 同时将删除与角色之间的关联关系, 不会删除角色
     *
     * @param identityId 身份ID
     */
    @Transactional(rollbackOn = Exception.class)
    public void delete(@NotNull IdentityId identityId) {
        identityService.delete(identityId);
    }

    /**
     * 禁用 指定的身份
     *
     * @param identityId 身份ID
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackOn = Exception.class)
    public void disable(@NotNull IdentityId identityId) throws BusinessException {
        Optional<Identity> optionalIdentity = this.jpaRepository.findById(identityId.getId());
        optionalIdentity.ifPresent(identity -> {
            identity.disable();
            this.jpaRepository.save(identity);
        });
    }

    /**
     * 启用指定的身份
     *
     * @param identityId 身份ID
     * @throws BusinessException 业务异常
     */
    @Transactional(rollbackOn = Exception.class)
    public void enable(@NotNull IdentityId identityId) throws BusinessException {
        Optional<Identity> optionalIdentity = this.jpaRepository.findById(identityId.getId());
        optionalIdentity.ifPresent(identity -> {
            identity.enable();
            this.jpaRepository.save(identity);
        });
    }

    /**
     * 为指定的身份绑定角色
     *
     * @param identityId 身份ID
     * @param roleIds    角色IDs
     * @throws BusinessException 如果绑定的角色错误或者不存在, 抛出此异常
     */
    @Transactional(rollbackOn = Exception.class)
    public void bindRoles(@NotNull IdentityId identityId, @NotEmpty Set<Long> roleIds) throws BusinessException {

    }

    /**
     * 移除指定身份的角色
     *
     * @param identityId 身份ID
     * @param roleId     角色ID
     * @throws BusinessException 如果移除失败, 则抛出此异常
     */
    @Transactional(rollbackOn = Exception.class)
    public void unbindRole(@NotNull IdentityId identityId, @NotNull Long roleId) throws BusinessException {

    }


    /**
     * 获取指定身份的所有角色
     *
     * @param identityId
     * @return
     */
    public Set<Long> getRoles(@NotNull IdentityId identityId) {
        return null;
    }
}
