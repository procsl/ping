package cn.procsl.ping.boot.domain.support.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.persistence.EntityManager;

import static cn.procsl.ping.boot.domain.support.utils.ReflectionUtils.findField;

/**
 * @author procsl
 * @date 2020/08/03
 */
@Slf4j
public class DomainRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends JpaRepositoryFactoryBean<T, S, ID> {

    /**
     * Creates a new {@link JpaRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public DomainRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        DomainRepositoryFactory domainRepositoryFactory = new DomainRepositoryFactory(entityManager);
        domainRepositoryFactory.setEntityPathResolver(
            findField(JpaRepositoryFactoryBean.class, this, "entityPathResolver", EntityPathResolver.class)
        );
        domainRepositoryFactory.setEscapeCharacter(
            findField(JpaRepositoryFactoryBean.class, this, "escapeCharacter", EscapeCharacter.class)
        );
        log.info("Create domain repository factory:{}", domainRepositoryFactory);
        return domainRepositoryFactory;
    }
}

