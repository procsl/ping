package cn.procsl.ping.boot.jpa;

import com.querydsl.jpa.Hibernate5Templates;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.hibernate.HibernateQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Slf4j
@AutoConfiguration(before = {JpaBaseConfiguration.class})
@ConditionalOnMissingBean(JpaDataAutoConfiguration.class)
@EnableJpaRepositories(bootstrapMode = BootstrapMode.LAZY, basePackages = "cn.procsl.ping.boot.jpa")
@EntityScan(basePackages = "cn.procsl.ping.boot.jpa")
public class JpaDataAutoConfiguration implements ApplicationContextAware {

    public final static String SYSTEM_ERROR_CODE_KEY = "SYSTEM_ERROR_CODE_KEY";

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        ContextHolder.setApplicationContext(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public JPQLQueryFactory jpaQueryFactory(EntityManager entityManager) {
        try {
            return new HibernateQueryFactory(Hibernate5Templates.DEFAULT, () -> entityManager.unwrap(Session.class));
        } catch (PersistenceException e) {
            return new JPAQueryFactory(entityManager);
        }

    }

}
