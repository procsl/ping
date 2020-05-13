package cn.procsl.ping.boot.data.business.repository;

import cn.procsl.ping.boot.data.business.entity.GeneralEntityTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author procsl
 * @date 2020/05/11
 */
@Repository
public interface GeneralEntityRepository extends JpaRepository<GeneralEntityTest, String>, QuerydslPredicateExecutor<GeneralEntityTest> {
}
