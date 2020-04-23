package cn.procsl.ping.boot.user.domain.rbac.repository;

import cn.procsl.ping.boot.user.domain.rbac.model.Identity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author procsl
 * @date 2020/04/19
 */
public interface UserRoleRepository extends JpaRepository<Identity, Long> {
}
