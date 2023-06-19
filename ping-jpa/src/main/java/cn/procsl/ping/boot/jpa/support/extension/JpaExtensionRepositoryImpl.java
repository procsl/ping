package cn.procsl.ping.boot.jpa.support.extension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;

@Slf4j
@RequiredArgsConstructor
class JpaExtensionRepositoryImpl<T, ID extends Serializable> implements JpaExtensionRepository<T, ID> {

    final EntityManager entityManager;

    @Override
    public <R> Page<R> queryAll(Specification<T> spec, Pageable pageable) {


        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = builder.createTupleQuery();

        Root<String> root = query.from(String.class);


        return null;
    }


}
