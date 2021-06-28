package cn.procsl.ping.boot.domain.support.executor;

import cn.procsl.ping.boot.domain.business.common.repository.PersistenceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;

/**
 * @author procsl
 * @date 2020/04/12
 */
@Slf4j
@NoRepositoryBean
class PersistenceExecutor<T, ID> implements PersistenceRepository<T, ID> {

    private final JpaEntityInformation<T, ?> entityInformation;

    private final EntityManager em;

    private final PersistenceProvider provider;

    private final CrudMethodMetadata metadata;

    private final EscapeCharacter escapeCharacter;

    public PersistenceExecutor(JpaEntityInformation<T, ?> entityInformation,
                               EntityManager em,
                               CrudMethodMetadata metadata,
                               EscapeCharacter escapeCharacter) {
        this.entityInformation = entityInformation;
        this.em = em;
        this.metadata = metadata;
        this.escapeCharacter = escapeCharacter;
        this.provider = PersistenceProvider.fromEntityManager(em);
    }


}
