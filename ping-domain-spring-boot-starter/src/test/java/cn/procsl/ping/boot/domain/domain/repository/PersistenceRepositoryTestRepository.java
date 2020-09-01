package cn.procsl.ping.boot.domain.domain.repository;

import cn.procsl.ping.boot.domain.domain.model.User;
import cn.procsl.ping.boot.domain.business.common.repository.PersistenceRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersistenceRepositoryTestRepository extends PersistenceRepository<User, String> {
}
