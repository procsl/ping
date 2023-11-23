package cn.procsl.ping.boot.ui.domain;

import cn.procsl.ping.boot.jpa.support.JpaSpecificationExecutorWithProjection;
import org.springframework.stereotype.Repository;

@Repository
public interface ComponentProjectionRepository extends JpaSpecificationExecutorWithProjection<Component, Long> {
}
