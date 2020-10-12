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
@Entity(name = "$domain:Dictionary")
@Table
@EntityListeners(DomainEventListener.class)
@DynamicUpdate
@Slf4j
public class Dictionary implements DomainEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "table_sequences")
    Long id;

}
