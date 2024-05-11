package cn.procsl.ping.boot.jpa;

import cn.procsl.ping.boot.jpa.domain.id.IdentifierSegmentRepository;
import cn.procsl.ping.boot.jpa.domain.id.TableIdentifierGenerator;
import cn.procsl.ping.boot.jpa.support.IdentifierGenerator;
import cn.procsl.ping.boot.jpa.support.extension.EnableJpaExtensionRepositories;
import cn.procsl.ping.boot.jpa.support.extension.JpaRepositoryFactoryCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Slf4j
@ConditionalOnMissingBean(JpaDataAutoConfiguration.class)
@EnableJpaExtensionRepositories(basePackages = "cn.procsl.ping.boot.jpa.domain")
@EntityScan(basePackages = "cn.procsl.ping.boot.jpa.domain")
public class JpaDataAutoConfiguration {


    @Bean
    public JpaRepositoryFactoryCustomizer jpaExtensionRepositoryFactoryCustomizer() {
        return new JpaRepositoryFactoryCustomizer();
    }


    @Bean
    public IdentifierGenerator<Long> identifierGenerator(IdentifierSegmentRepository repository,
                                                         TransactionTemplate transactionTemplate) {
        return new TableIdentifierGenerator(repository, transactionTemplate, 10, 1);
    }


}
