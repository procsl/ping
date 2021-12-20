package cn.procsl.ping.boot.user.rbac;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "u_role")
@NoArgsConstructor
@RepositoryCreator
public class Role extends AbstractPersistable<Long> implements Serializable {

    @Column(unique = true)
    String name;

    @ElementCollection
    @CollectionTable(name = "u_role_permission")
    Set<Permission> permissions;

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


    public Role(String name, Collection<String> permissions) {
        this.name = name;
        this.changePermissions(permissions);
    }
}
