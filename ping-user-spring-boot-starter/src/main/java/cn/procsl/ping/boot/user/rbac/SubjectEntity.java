package cn.procsl.ping.boot.user.rbac;

import lombok.*;
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
@AllArgsConstructor
@Builder
public class SubjectEntity extends AbstractPersistable<Long> implements Subject<RoleEntity> {

    Long subjectId;

    @OneToMany
    Set<RoleEntity> roles;

}
