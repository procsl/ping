package cn.procsl.ping.boot.domain.config;

import cn.procsl.ping.boot.domain.naming.LowCasePhysicalNamingStrategy;
import cn.procsl.ping.boot.domain.naming.NameImplicitNamingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Configuration
@ConditionalOnMissingBean(DataAutoConfiguration.class)
@EnableConfigurationProperties({DataProperties.class, HibernateProperties.class})
@AutoConfigureBefore(JpaBaseConfiguration.class)
@Slf4j
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.domain.business.repository")
@EntityScan(basePackages = "cn.procsl.ping.boot.data.business.entity")
public class DataAutoConfiguration {

    final DataProperties dataProperties;

    final HibernateProperties hibernateProperties;

    @Bean
    @ConditionalOnMissingBean
    public NameImplicitNamingStrategy nameImplicitNamingStrategy() {
        return new NameImplicitNamingStrategy(dataProperties);
    }


    @Bean
    @ConditionalOnMissingBean
    public LowCasePhysicalNamingStrategy lowCasePhysicalNamingStrategy() {
        return new LowCasePhysicalNamingStrategy(dataProperties);
    }

    public DataAutoConfiguration(DataProperties dataProperties, HibernateProperties hibernateProperties) {
        this.dataProperties = dataProperties;
        this.hibernateProperties = hibernateProperties;
        String phy = hibernateProperties.getNaming().getImplicitStrategy();

        if (phy == null || phy.isEmpty()) {
            String name = LowCasePhysicalNamingStrategy.class.getName();
            log.info("spring.jpa.hibernate.naming.physical-strategy:{}", name);
            hibernateProperties.getNaming().setPhysicalStrategy(name);
        }

        String imp = hibernateProperties.getNaming().getImplicitStrategy();
        if (imp == null || imp.isEmpty()) {
            String name = NameImplicitNamingStrategy.class.getName();
            log.info("spring.jpa.hibernate.naming.implicit-strategy:{}", name);
            hibernateProperties.getNaming().setImplicitStrategy(name);
        }

    }
}
