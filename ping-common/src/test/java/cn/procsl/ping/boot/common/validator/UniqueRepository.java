package cn.procsl.ping.boot.common.validator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniqueRepository extends JpaRepository<Unique, Long> {
}
