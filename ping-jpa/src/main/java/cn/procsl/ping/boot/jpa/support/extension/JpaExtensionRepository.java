package cn.procsl.ping.boot.jpa.support.extension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface JpaExtensionRepository<T, ID> extends Repository<T, ID> {

    <R> Page<R> queryAll(Specification<T> spec, Pageable pageable);

}
