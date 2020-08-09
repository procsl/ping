package cn.procsl.ping.boot.domain.domain.repository;

import cn.procsl.ping.boot.domain.domain.entity.User;
import cn.procsl.ping.boot.domain.support.jpa.PersistenceRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersistenceRepositoryTestRepository extends PersistenceRepository<User, String> {
}
