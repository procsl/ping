package cn.procsl.ping.boot.user.domain.rbac.repository;

import cn.procsl.ping.boot.user.domain.rbac.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import javax.inject.Named;

/**
 * @author procsl
 * @date 2020/04/09
 */
public interface RoleRepository extends JpaRepository<Role, Long>, QuerydslPredicateExecutor<Role>  {
}
