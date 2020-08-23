package cn.procsl.ping.boot.domain.business.common.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * @author procsl
 * @date 2020/04/12
 */
@NoRepositoryBean
public interface PersistenceRepository<T, ID> extends Repository<T, ID> {

}
