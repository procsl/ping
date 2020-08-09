package cn.procsl.ping.boot.domain.domain.repository;

import cn.procsl.ping.boot.domain.domain.entity.TreeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author procsl
 * @date 2020/07/31
 */
@Repository
public interface TreeEntityTestRepository extends JpaRepository<TreeEntity, String>,
        QuerydslPredicateExecutor<TreeEntity> {
}
