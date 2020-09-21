package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.domain.annotation.CreateRepository;
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
@EqualsAndHashCode(exclude = "roles")
@ToString
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"space", "parentId"})})
@Entity(name = "r_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)// for jpa
@Slf4j
@EntityListeners(DomainEventListener.class)
@CreateRepository
public class Subject implements DomainEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "generator")
    @Access(AccessType.PROPERTY)
    Long id;

    @ManyToMany
    Set<Role> roles;

    public Subject(Collection<Role> roles) {
        this.roles = CollectionUtils.createAndAppend(this.roles, roles);
    }

    public void changeRoles(Collection<Role> roles) {
        CollectionUtils.nullSafeClear(this.roles);
        this.roles = CollectionUtils.createAndAppend(this.roles, roles);
    }
}
