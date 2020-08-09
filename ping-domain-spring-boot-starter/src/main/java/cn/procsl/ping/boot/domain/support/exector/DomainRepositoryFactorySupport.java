package cn.procsl.ping.boot.domain.support.exector;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.persistence.EntityManager;

@RequiredArgsConstructor
public class DomainRepositoryFactorySupport extends RepositoryFactorySupport {

    private final EntityManager entityManager;

    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return (JpaEntityInformation<T, ID>) JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager);
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation metadata) {
        return new AdjacencyTreeRepositoryImpl<>(this.entityManager);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return AdjacencyTreeRepositoryImpl.class;
    }
}
