package cn.procsl.ping.boot.jpa.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Repository;

@Indexed
@Repository
public interface StandRepository extends JpaRepository<TestEntity, Long> {
}
