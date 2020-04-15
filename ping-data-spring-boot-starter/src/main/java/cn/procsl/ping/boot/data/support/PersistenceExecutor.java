package cn.procsl.ping.boot.data.support;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * @author procsl
 * @date 2020/04/12
 */
@NoRepositoryBean
public interface PersistenceExecutor<T, ID> extends Repository<T, ID> {

}
