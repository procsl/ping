package cn.procsl.ping.admin.config;

import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class AdminConfigure {

    @Bean
    public JPQLQueryFactory jpaQueryFactory(@Autowired EntityManager entityManager) {

//        try {
//            return new HibernateQueryFactory(Hibernate5Templates.DEFAULT, () -> entityManager.unwrap(Session.class));
//        } catch (PersistenceException e) {
        return new JPAQueryFactory(entityManager);
//        }

    }
}
