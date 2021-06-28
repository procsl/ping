package cn.procsl.ping.boot.domain.domain.repository;

import cn.procsl.ping.boot.domain.domain.model.Tree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author procsl
 * @date 2020/07/31
 */
@Repository
public interface TreeTestRepository extends JpaRepository<Tree, Long>,
    QuerydslPredicateExecutor<Tree> {
}
