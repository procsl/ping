package cn.procsl.ping.boot.domain.domain.repository;

import cn.procsl.ping.boot.domain.business.common.repository.QueryDslPersistenceRepository;
import cn.procsl.ping.boot.domain.domain.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryDslPersistenceRepositoryTestRepository extends QueryDslPersistenceRepository<User, String> {
}
