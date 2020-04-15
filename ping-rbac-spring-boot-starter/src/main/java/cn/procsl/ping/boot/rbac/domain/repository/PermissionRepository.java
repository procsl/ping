package cn.procsl.ping.boot.rbac.domain.repository;

import cn.procsl.ping.boot.rbac.domain.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author procsl
 * @date 2020/04/09
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, QuerydslPredicateExecutor<Permission> {
}
