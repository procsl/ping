package cn.procsl.ping.boot.user.rbac;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "s_role")
@NoArgsConstructor
public class Role extends AbstractPersistable<Long> implements Serializable {

    String name;

    @ElementCollection
    Set<Permission> permissions;

    public void changePermissions(Collection<Permission> permissions) {
        if (!permissions.isEmpty()) {
            this.permissions.clear();
            this.permissions.addAll(permissions);
        }
    }

    public Role(String name, Collection<Permission> permissions) {
        this.name = name;
        if (permissions instanceof HashSet) {
            this.permissions = (Set<Permission>) permissions;
            return;
        }
        this.permissions = new HashSet<>(permissions);
    }
}
