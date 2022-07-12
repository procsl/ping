package cn.procsl.ping.boot.admin.domain.rbac;

import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@RepositoryCreator
@Table(name = "i_role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends AbstractPersistable<Long> implements Serializable {

    String name;

    @ManyToMany
    List<Permission> permissions;

    public Role(String name) {
        this(name, new ArrayList<>());
    }

    public Role(String name, Collection<Permission> permissions) {
        this.name = name;
        this.changePermissions(permissions);
    }

    public void changePermissions(Collection<Permission> permissions) {
        if (permissions == null) {
            this.permissions.clear();
            return;
        }

        if (this.permissions == null) {
            this.permissions = new ArrayList<>(permissions.size());
        }

        if (!this.permissions.isEmpty()) {
            this.permissions.clear();
        }
        this.permissions.addAll(permissions);
    }

    public void change(@Nullable String name, @Nullable Collection<Permission> permissions) {
        boolean bool = name == null || name.isEmpty();
        if (!bool) {
            this.name = name;
        }

        if (permissions != null) {
            this.changePermissions(permissions);
        }
    }

    public void addPermission(Permission permission) {
        if (this.permissions == null) {
            this.permissions = new ArrayList<>();
        }
        this.permissions.add(permission);
    }

    public void addPermissions(Collection<Permission> permissions) {
        if (this.permissions == null) {
            this.permissions = new ArrayList<>();
        }
        this.permissions.addAll(permissions);
    }

}
