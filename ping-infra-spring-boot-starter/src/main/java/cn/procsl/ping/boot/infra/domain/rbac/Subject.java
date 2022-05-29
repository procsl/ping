package cn.procsl.ping.boot.infra.domain.rbac;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "i_subject")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RepositoryCreator
public class Subject extends AbstractPersistable<Long> implements Serializable {

    Long subject;

    @ManyToMany
    Set<Role> roles;

    public Subject(Long subject) {
        this.subject = subject;
    }

    void addRoles(Collection<Role> roles) {
        if (this.roles == null) {
            this.roles = new HashSet<>(roles);
        } else {
            this.roles.clear();
            this.roles.addAll(roles);
        }
    }

}
