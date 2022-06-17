package cn.procsl.ping.boot.domain.jpastramer;

import com.speedment.jpastreamer.application.JPAStreamer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

@Configuration
public class JpaStreamStupidConfig {

    @Autowired
    EntityManager entityManager;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Bean
    public JPAStreamer jpaStreamer() {
        return JPAStreamer.createJPAStreamerBuilder(new EntityManagerFactory() {


            @Override
            public EntityManager createEntityManager() {
                return JpaStreamStupidConfig.this.entityManager;
            }

            @Override
            public EntityManager createEntityManager(Map map) {
                return JpaStreamStupidConfig.this.entityManager;
            }

            @Override
            public EntityManager createEntityManager(SynchronizationType synchronizationType) {
                return JpaStreamStupidConfig.this.entityManager;
            }

            @Override
            public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
                return JpaStreamStupidConfig.this.entityManager;
            }

            @Override
            public CriteriaBuilder getCriteriaBuilder() {
                return JpaStreamStupidConfig.this.entityManager.getCriteriaBuilder();
            }

            @Override
            public Metamodel getMetamodel() {
                return JpaStreamStupidConfig.this.entityManager.getMetamodel();
            }

            @Override
            public boolean isOpen() {
                return JpaStreamStupidConfig.this.entityManager.isOpen();
            }

            @Override
            public void close() {
            }

            @Override
            public Map<String, Object> getProperties() {
                return JpaStreamStupidConfig.this.entityManager.getProperties();
            }

            @Override
            public Cache getCache() {
                return JpaStreamStupidConfig.this.entityManagerFactory.getCache();
            }

            @Override
            public PersistenceUnitUtil getPersistenceUnitUtil() {
                return JpaStreamStupidConfig.this.entityManagerFactory.getPersistenceUnitUtil();
            }

            @Override
            public void addNamedQuery(String name, Query query) {
                JpaStreamStupidConfig.this.entityManagerFactory.addNamedQuery(name, query);
            }

            @Override
            public <T> T unwrap(Class<T> cls) {
                return JpaStreamStupidConfig.this.entityManager.unwrap(cls);
            }

            @Override
            public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
                JpaStreamStupidConfig.this.entityManagerFactory.addNamedEntityGraph(graphName, entityGraph);
            }
        }).build();
    }

}
