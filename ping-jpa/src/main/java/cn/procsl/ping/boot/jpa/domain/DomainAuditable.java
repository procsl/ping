package cn.procsl.ping.boot.jpa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.Instant;

//@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Embeddable
public class DomainAuditable implements Serializable {

    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdDate;

    @LastModifiedBy
    @Setter
    private Long lastModifiedBy;

    @LastModifiedDate
    private Instant lastModifiedDate;


}
