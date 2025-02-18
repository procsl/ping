package cn.procsl.ping.boot.jpa.support.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubEntityRepo extends JpaRepository<SubEntity, Long> {
}
