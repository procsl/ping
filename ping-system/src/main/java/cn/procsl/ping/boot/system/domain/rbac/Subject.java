package cn.procsl.ping.boot.system.domain.rbac;

import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "s_subject")
@NoArgsConstructor
@RepositoryCreator
public class Subject implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;

    @Column(updatable = false)
    Long subject;

    @ManyToMany
    Set<Role> roles;

    public Subject(Long subject) {
        this.subject = subject;
        this.roles = new HashSet<>();
    }


    public void grant(Collection<Role> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void grant(Role... roles) {
        if (roles == null || roles.length == 0) {
            this.roles.clear();
            return;
        }
        this.roles.addAll(List.of(roles));
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    @Transient
    public Collection<Permission> getPermissions() {
        HashSet<Permission> hashset = new HashSet<>();
        for (Role role : roles) {
            hashset.addAll(role.getPermissions());
        }
        return Collections.unmodifiableCollection(hashset);
    }

    @Transient
    public <T extends Permission> Collection<T> getPermissions(Class<T> clazz) {
        HashSet<T> hashset = new HashSet<>();
        for (Role role : roles) {
            hashset.addAll(role.getPermissions(clazz));
        }
        return Collections.unmodifiableCollection(hashset);
    }

}
