package cn.procsl.ping.boot.jpa.support.extension;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface JpaExtensionRepository<T, ID> extends Repository<T, ID> {
}
