package cn.procsl.ping.boot.admin.domain.rbac;

import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "i_subject")
@NoArgsConstructor
@RepositoryCreator
public class Subject extends AbstractPersistable<Long> implements Serializable {

    Long subject;

    @ManyToMany
    Set<Role> roles;

    public Subject(Long subject) {
        this.subject = subject;
    }

    public Subject(Long subject, Collection<Role> roles) {
        this.subject = subject;
        this.putRoles(roles);
    }

    void putRoles(Collection<Role> roles) {
        if (this.roles == null) {
            this.roles = new HashSet<>(roles);
        } else {
            this.roles.clear();
            this.roles.addAll(roles);
        }
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

}
