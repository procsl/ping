package cn.procsl.ping.boot.domain.business;

import cn.procsl.ping.business.domain.DomainEntity;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/**
 * 通用实体
 *
 * @author procsl
 * @date 2020/04/19
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public interface CommonEntity extends DomainEntity {

    @Version
    @ColumnDefault("0")
    @Column(nullable = false, insertable = false)
    Long getVersion();

    @CreatedDate
    @Column(updatable = false, nullable = false)
    @OrderBy
    Long getCreateAt();
}
