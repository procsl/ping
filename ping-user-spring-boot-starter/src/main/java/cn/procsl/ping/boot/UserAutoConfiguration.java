package cn.procsl.ping.boot;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@RequiredArgsConstructor
@EntityScan(basePackages = {"cn.procsl.ping.boot.user", "cn.procsl.ping.boot.account", "cn.procsl.ping.boot.rbac", "cn.procsl.ping.boot.system"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"cn.procsl.ping.boot.user", "cn.procsl.ping.boot.account", "cn.procsl.ping.boot.rbac", "cn.procsl.ping.boot.system"}, bootstrapMode = BootstrapMode.LAZY)
public class UserAutoConfiguration {


//    @Bean
//    public AccountService accountService(final JpaRepository<Account, Long> jpaRepository) {
//        return new AccountService(jpaRepository);
//    }
//
//    @Bean
//    public ConfigureService configureService(final JpaRepository<cn.procsl.ping.boot.system.Configuration, Long> configureLongJpaRepository) {
//        return new ConfigureService(configureLongJpaRepository);
//    }
//
//    @Bean
//    public AccessControlService accessControlService(final JpaRepository<Subject, Long> subjectRepository, final RoleRepository roleRepository) {
//        return new AccessControlService(subjectRepository, roleRepository);
//    }
//
//    @Bean
//    public UserService userService(final AccountFacade accountFacade, final AccessControlFacade accessControlFacade, final ConfigureFacade configureFacade, final JpaRepository<User, Long> jpaRepository
//    ) {
//        return new UserService(accountFacade, accessControlFacade, configureFacade, jpaRepository);
//    }
//
//    @Bean
//    public AccountFacade accountFacade(AccountService accountService) {
//        return new InnerAccountFacadeImpl(accountService);
//    }
//
//    @Bean
//    public AccessControlFacade accessControlFacade(final AccessControlService accessControlService) {
//        return new InnerAccessControlFacadeImpl(accessControlService);
//    }
//
//    @Bean
//    public ConfigureFacade configureFacade(final ConfigureService configureService) {
//        return new InnerConfigureFacadeImpl(configureService);
//    }

}
