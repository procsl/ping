package cn.procsl.ping.boot.user.rbac;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "u_subject")
@NoArgsConstructor
@RepositoryCreator
@AllArgsConstructor
public class Subject extends AbstractPersistable<Long> {

    Long subjectId;

    // user
    String type;

    @OneToMany
    Set<Role> roles;

}
