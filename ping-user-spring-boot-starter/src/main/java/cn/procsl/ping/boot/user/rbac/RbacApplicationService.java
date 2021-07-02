package cn.procsl.ping.boot.user.rbac;


import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Collection;

@Service
@Path("roles")
@RequiredArgsConstructor
public class RbacApplicationService {

    final JpaRepository<Role, Long> roleJpaRepository;

    /**
     * 创建角色
     *
     * @param name        角色名称
     * @param permissions 权限
     */
    @POST
    @Transactional(rollbackOn = Exception.class)
    public Long createRole(String name, Collection<String> permissions) throws RbacException {
        this.checkPermissions(permissions);
        Collection<Permission> perms = this.convertPermissions(permissions);
        Role role = roleJpaRepository.save(new Role(name, perms));
        return role.getId();
    }

    public Collection<Permission> convertPermissions(Collection<String> permissions) {
        return null;
    }

    public void checkPermissions(Collection<String> permissions) throws RbacException {

    }

}
