package cn.procsl.ping.boot.data.business.audit;

import cn.procsl.ping.business.domain.DomainEntity;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/**
 * 审计实体
 *
 * @author procsl
 * @date 2020/04/19
 */
@Data
@EntityListeners(AuditingEntityListener.class)
@Embeddable
public class AuditEntity implements DomainEntity {

    @Version
    @ColumnDefault("0")
    @Column(nullable = false, insertable = false)
    private Long version;


    @CreatedDate
    @Column(updatable = false, nullable = false)
    @OrderBy
    private Long createAt;
}
