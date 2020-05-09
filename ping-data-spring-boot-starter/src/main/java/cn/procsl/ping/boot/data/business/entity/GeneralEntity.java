package cn.procsl.ping.boot.data.business.entity;

import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/04/19
 */
@Data
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class GeneralEntity implements Serializable {

    public static final int GENERAL_ENTITY_ID_LENGTH = 20;

    /**
     * 版本号
     */
    @Version
    @ColumnDefault("0")
    @Column(nullable = false, insertable = false)
    private Long version;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(updatable = false, nullable = false)
    @OrderBy
    private Long createAt;

}
