package cn.procsl.ping.boot.user.config;

import cn.procsl.ping.boot.user.rbac.RbacApplicationService;
import cn.procsl.ping.boot.user.rbac.RbacException;
import cn.procsl.ping.boot.user.rbac.Role;
import cn.procsl.ping.boot.user.rbac.VerifyPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collection;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@RequiredArgsConstructor
@EntityScan(basePackages = "cn.procsl.ping.boot.user")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.user.repository")
@EnableTransactionManagement
public class UserAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(VerifyPermissionService.class)
    public SimpleVerifyPermissionService InnerVerifyPermissionService() {
        return new SimpleVerifyPermissionService();
    }

    @Bean
    @ConditionalOnMissingBean(RbacApplicationService.class)
    public RbacApplicationService rbacApplicationService(JpaRepository<Role, Long> roleJpaRepository, VerifyPermissionService verifyPermissionServiceProvider) {
        return new RbacApplicationService(roleJpaRepository, verifyPermissionServiceProvider);
    }

    static final class SimpleVerifyPermissionService implements VerifyPermissionService {
        @Override
        public void verify(Collection<String> permissions) throws RbacException {
        }
    }

}
