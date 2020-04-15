package cn.procsl.ping.boot.rbac.domain.repository;

import cn.procsl.ping.boot.rbac.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author procsl
 * @date 2020/04/08
 */
@Repository
public interface UserRepository extends JpaRepository<User, String>, QuerydslPredicateExecutor<User> {
}
