package cn.procsl.ping.boot.rbac;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor {

    Set<RoleEntity> findRolesByNameIn(Collection<String> roleNames);

}
