package cn.procsl.ping.boot.domain.domain.repository;

import cn.procsl.ping.boot.domain.domain.entity.User;
import cn.procsl.ping.boot.domain.support.querydsl.QueryDslPersistenceRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryDslPersistenceRepositoryTestRepository extends QueryDslPersistenceRepository<User, String> {
}
