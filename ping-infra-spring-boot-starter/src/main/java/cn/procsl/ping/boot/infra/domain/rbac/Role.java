package cn.procsl.ping.boot.infra.domain.rbac;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.annotation.Nullable;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity(name = "role")
@Table(name = "i_role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
public class Role extends AbstractPersistable<Long> implements Serializable {

    protected static Set<String> empty = Collections.emptySet();

    String name;

    @ElementCollection
    @CollectionTable(name = "i_role_permission")
    Set<Permission> permissions;

    public Role(String name) {
        this(name, empty);
    }

    public Role(String name, Collection<String> permissions) {
        this.name = name;
        this.changePermissions(permissions);
    }

    public static Permission createPermission(String permission) {
        return new Permission(permission);
    }

    public void changePermissions(Collection<String> permissions) {

        if (this.permissions == null) {
            this.permissions = new HashSet<>(permissions.size());
        }

        if (!this.permissions.isEmpty()) {
            this.permissions.clear();
        }
        for (String permission : permissions) {
            this.permissions.add(createPermission(permission));
        }
    }

    public void change(@Nullable String name, @Nullable Collection<String> permissions) {
        boolean bool = name == null || name.isEmpty();
        if (!bool) {
            this.name = name;
        }

        if (permissions != null) {
            this.changePermissions(permissions);
        }
    }
}
