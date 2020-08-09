package cn.procsl.ping.boot.domain.support.exector;

import cn.procsl.ping.boot.domain.support.business.AdjacencyTreeRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;

import javax.persistence.EntityManager;

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
        val fragments = super.getRepositoryFragments(metadata);
        boolean bool = AdjacencyTreeRepository.class.isAssignableFrom(metadata.getRepositoryInterface());
        if (bool) {
            Object fragment = getTargetRepositoryViaReflection(AdjacencyTreeRepositoryImpl.class);
            return fragments.append(RepositoryFragment.implemented(fragment));
        }
        return fragments;
    }
}
