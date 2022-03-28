package cn.procsl.ping.boot.rbac;

import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "u_role")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity extends AbstractPersistable<Long> implements Role {

    String name;

    @ElementCollection
    @CollectionTable(name = "u_role_permission")
    Set<Permission> permissions;

    public RoleEntity(String name) {
        this.name = name;
    }

    public RoleEntity(String name, Collection<String> permissions) {
        this.name = name;
        this.changePermissions(permissions);
    }

    public void changePermissions(Collection<String> permissions) {

        if (this.permissions == null) {
            this.permissions = new HashSet<>(permissions.size());
        }

        if (!this.permissions.isEmpty()) {
            this.permissions.clear();
        }
        for (String permission : permissions) {
            this.permissions.add(new Permission(permission));
        }
    }
}
