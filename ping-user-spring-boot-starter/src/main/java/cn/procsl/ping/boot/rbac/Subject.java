package cn.procsl.ping.boot.rbac;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "u_subject")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RepositoryCreator
public class Subject extends AbstractPersistable<Long> implements Serializable {

    Long subjectId;

    @OneToMany
    Set<Role> roles;

}
