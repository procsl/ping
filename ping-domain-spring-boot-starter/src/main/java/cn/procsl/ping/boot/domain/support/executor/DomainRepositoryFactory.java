package cn.procsl.ping.boot.domain.support.executor;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.support.jpa.PersistenceRepository;
import cn.procsl.ping.boot.domain.support.querydsl.QueryDslPersistenceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static cn.procsl.ping.boot.domain.support.utils.ReflectionUtils.findField;

@Slf4j
public class DomainRepositoryFactory extends JpaRepositoryFactory {

    /**
     * Creates a new {@link JpaRepositoryFactory}.
     *
     * @param entityManager must not be {@literal null}
     */
    public DomainRepositoryFactory(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected RepositoryComposition.RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata) {
        log.info("build domain repositories");
        RepositoryComposition.RepositoryFragments fragments = super.getRepositoryFragments(metadata);

        JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(metadata.getDomainType());

//        QueryExtractor queryExtractor = findField(JpaRepositoryFactory.class,
//                this, "extractor", QueryExtractor.class);

        EntityManager entityManager = findField(JpaRepositoryFactory.class,
                this, "entityManager", EntityManager.class);

        EntityPathResolver entityPathResolver = findField(JpaRepositoryFactory.class,
                this, "entityPathResolver", EntityPathResolver.class);

        CrudMethodMetadata crudMethodMetadata = this.findCrudMethodMetadata();

        EscapeCharacter escapeCharacter = findField(JpaRepositoryFactory.class,
                this, "escapeCharacter", EscapeCharacter.class);

        if (AdjacencyTreeRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
            Object target = getTargetRepositoryViaReflection(AdjacencyTreeExecutor.class,
                    entityInformation,
                    entityManager,
                    escapeCharacter,
                    crudMethodMetadata
            );
            fragments = fragments.append(RepositoryFragment.implemented(target));
        }

        if (PersistenceRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
            Object target = getTargetRepositoryViaReflection(PersistenceExecutor.class,
                    entityInformation,
                    entityManager,
                    crudMethodMetadata,
                    escapeCharacter
            );
            fragments = fragments.append(RepositoryFragment.implemented(target));
        }

        if (QueryDslPersistenceRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
            Object target = getTargetRepositoryViaReflection(QueryDslPersistenceExecutor.class,
                    entityInformation,
                    entityManager,
                    crudMethodMetadata,
                    escapeCharacter,
                    entityPathResolver
            );
            fragments = fragments.append(RepositoryFragment.implemented(target));
        }

        return fragments;
    }

    private CrudMethodMetadata findCrudMethodMetadata() {
        Object crudMethodMetadataPostProcessor = findField(JpaRepositoryFactory.class,
                this, "crudMethodMetadataPostProcessor", null);

        Method method = ReflectionUtils.findMethod(crudMethodMetadataPostProcessor.getClass(), "getCrudMethodMetadata");
        try {
            method.setAccessible(true);
            CrudMethodMetadata tmp = (CrudMethodMetadata) method.invoke(crudMethodMetadataPostProcessor);
            method.setAccessible(false);
            return tmp;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("method getCrudMethodMetadata not found.", e);
        }
    }


}
