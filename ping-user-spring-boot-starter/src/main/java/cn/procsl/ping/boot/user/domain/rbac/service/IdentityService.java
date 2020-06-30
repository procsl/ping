package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.domain.rbac.entity.Identity;
import cn.procsl.ping.boot.user.domain.rbac.entity.IdentityId;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;

/**
 * 领域服务
 *
 * @author procsl
 * @date 2020/04/19
 */
@Named
@Singleton
public class IdentityService {

    @Inject
    private JpaRepository<Identity, Long> identityJpaRepository;

    public IdentityId create() throws BusinessException {
        Identity identity = Identity
                .creator()
                .active(true)
                .roles(Collections.singleton(Role.EMPTY_ROLE_ID))
                .done();
        identityJpaRepository.save(identity);
        return new IdentityId(identity.getId());
    }

    public void delete(IdentityId identityId) throws BusinessException {
        identityJpaRepository.deleteById(identityId.getId());
    }
}
