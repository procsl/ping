package cn.procsl.ping.boot.domain.domain.model;

import cn.procsl.ping.boot.domain.support.executor.DomainEventListener;
import cn.procsl.ping.business.domain.DomainEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * querydsl冲突测试
 */
@Data
@Entity(name = "Dict")
@Table(name = "dict")
@EntityListeners(DomainEventListener.class)
@DynamicUpdate
@Slf4j
public class Dictionary implements DomainEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
    @SequenceGenerator(allocationSize = 500, name = "generator", sequenceName = "tree_entity_seq")
    @Column(updatable = false, nullable = false)
    Long id;

}
