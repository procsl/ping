package cn.procsl.ping.boot.user.domain.rbac.repository;

import cn.procsl.ping.boot.user.domain.rbac.model.Identity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.ColumnResult;

/**
 * @author procsl
 * @date 2020/04/19
 */
public interface UserRoleRepository extends JpaRepository<Identity, Long> {
}
