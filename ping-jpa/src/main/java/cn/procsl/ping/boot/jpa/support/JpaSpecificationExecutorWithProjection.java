package cn.procsl.ping.boot.jpa.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

@NoRepositoryBean
public interface JpaSpecificationExecutorWithProjection<T, ID extends Serializable> extends org.springframework.data.repository.Repository<T, ID> {

    <R> Optional<R> findOne(Specification<T> spec, Class<R> projectionClass);

    <R> Page<R> findAll(Specification<T> spec, Class<R> projectionClass, Pageable pageable);

}