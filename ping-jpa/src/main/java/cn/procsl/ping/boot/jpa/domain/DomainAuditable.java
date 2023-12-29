package cn.procsl.ping.boot.jpa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

//@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Embeddable
public class DomainAuditable implements Serializable {

    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @Column(updatable = false)
    private Date createdDate;

    @LastModifiedBy
    @Setter
    private Long lastModifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date lastModifiedDate;

    public @NonNull Optional<Long> getCreatedBy() {
        return Optional.ofNullable(createdBy);
    }

    public @NonNull Optional<Date> getCreatedDate() {
        return Optional.ofNullable(createdDate);
    }

    public void setCreatedDate(Date creationDate) {
        this.createdDate = creationDate;
    }

    public @NonNull Optional<Long> getLastModifiedBy() {
        return Optional.ofNullable(lastModifiedBy);
    }

    public @NonNull Optional<Date> getLastModifiedDate() {
        return Optional.ofNullable(this.lastModifiedDate);
    }

}
