package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.apt.annotation.RepositoryCreator;
import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.boot.domain.support.executor.DomainEventListener;
import cn.procsl.ping.business.domain.DomainEntity;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Setter(AccessLevel.PROTECTED) // for jpa
@Getter
@EqualsAndHashCode(exclude = "role")
@ToString(exclude = "role")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "userId"))
@Entity(name = "$user:subject")
@NoArgsConstructor(access = AccessLevel.PROTECTED)// for jpa
@Slf4j
@EntityListeners(DomainEventListener.class)
@RepositoryCreator
public class Subject implements DomainEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "table_sequences")
    @Access(AccessType.PROPERTY)
    Long id;

    @Column(length = UUID_LENGTH, updatable = false, nullable = false)
    String userId;

    @JoinTable(name = "$user:subject_role")
    @ManyToMany
    Set<Role> role;

    public Subject(String userId, Collection<Role> roles) {
        this.role = CollectionUtils.createAndAppend(this.role, roles);
        this.userId = userId;
    }

    public void changeRoles(Collection<Role> roles) {
        CollectionUtils.nullSafeClear(this.role);
        this.role = CollectionUtils.createAndAppend(this.role, roles);
    }
}
