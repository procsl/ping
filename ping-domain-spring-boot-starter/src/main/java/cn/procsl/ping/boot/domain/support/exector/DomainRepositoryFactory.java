package cn.procsl.ping.boot.domain.support.exector;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.lang.Nullable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author procsl
 * @date 2020/08/03
 */
public class DomainRepositoryFactory<T extends Repository<S, ID>, S, ID>
        extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    private @Nullable EntityManager entityManager;

    private EntityPathResolver entityPathResolver;

    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;

    /**
     * Creates a new {@link TransactionalRepositoryFactoryBeanSupport} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    protected DomainRepositoryFactory(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected DomainRepositoryFactorySupport doCreateRepositoryFactory() {
        DomainRepositoryFactorySupport support = new DomainRepositoryFactorySupport(this.entityManager);
//        support.setEntityPathResolver(entityPathResolver);
//        support.setEscapeCharacter(escapeCharacter);
        return support;
    }


    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Inject
    public void setEntityPathResolver(ObjectProvider<EntityPathResolver> resolver) {
        this.entityPathResolver = resolver.getIfAvailable(() -> SimpleEntityPathResolver.INSTANCE);
    }

}

