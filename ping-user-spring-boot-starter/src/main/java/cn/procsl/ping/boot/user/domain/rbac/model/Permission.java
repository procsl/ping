package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.apt.annotation.RepositoryCreator;
import cn.procsl.ping.boot.domain.support.executor.DomainEventListener;
import cn.procsl.ping.business.domain.DomainEntity;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Set;

@Setter(AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(exclude = {"role"})
@ToString(exclude = {"role"})
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"operator", "resource"})})
@Entity(name = "$user:permission")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@EntityListeners(DomainEventListener.class)
@RepositoryCreator
@Immutable
public class Permission implements Target, DomainEntity<Long> {

    final public static int OPS_LEN = 5;

    final public static int RES_NO_LEN = 32;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "table_sequences")
    @Access(AccessType.PROPERTY)
    Long id;

    @Column(length = OPS_LEN, nullable = false)
    @Setter(AccessLevel.PUBLIC)
    String operator;

    @Column(length = RES_NO_LEN, nullable = false)
    String resource;

    @ManyToMany
    @JoinTable(name = "$user:role_permission")
    Set<Role> role;

    public Permission(@NonNull String resource,
                      @NonNull String operator
    ) {
        this.operator = operator;
        this.resource = resource;
    }

    public Permission(@NonNull Target target) {
        this(target.getResource(), target.getOperator());
    }

}

