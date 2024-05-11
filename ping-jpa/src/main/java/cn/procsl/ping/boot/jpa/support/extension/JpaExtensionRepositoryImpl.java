package cn.procsl.ping.boot.jpa.support.extension;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.io.Serializable;

@Slf4j
class JpaExtensionRepositoryImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID>
        implements JpaExtensionRepository<T, ID> {

    public JpaExtensionRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    public JpaExtensionRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
    }

    @Override
    public <R> Page<R> queryAll(Specification<T> spec, Pageable pageable) {


//        CriteriaBuilder builder = .getCriteriaBuilder();
//
//        CriteriaQuery<Tuple> query = builder.createTupleQuery();
//
//        Root<String> root = query.from(String.class);


        return null;
    }


}
