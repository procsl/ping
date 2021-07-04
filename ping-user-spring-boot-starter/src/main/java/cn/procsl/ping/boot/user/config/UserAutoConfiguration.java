package cn.procsl.ping.boot.user.config;

import cn.procsl.ping.boot.user.rbac.RbacException;
import cn.procsl.ping.boot.user.rbac.VerifyPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collection;

@Configuration
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@RequiredArgsConstructor
@EntityScan(basePackages = "cn.procsl.ping.boot.user")
@EnableJpaRepositories("cn.procsl.ping.boot.user")
@EnableTransactionManagement
public class UserAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SimpleVerifyPermissionService.class)
    public SimpleVerifyPermissionService InnerVerifyPermissionService() {
        return new SimpleVerifyPermissionService();
    }

    static final class SimpleVerifyPermissionService implements VerifyPermissionService {
        @Override
        public void verify(Collection<String> permissions) throws RbacException {
        }
    }

}
