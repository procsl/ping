package cn.procsl.ping.boot.jpa.support;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Auditable;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class DomainAuditable extends AbstractPersistable<Long> implements Auditable<Long, Long, LocalDateTime> {

    @CreatedBy
    @Setter
    private Long createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;

    @LastModifiedBy
    @Setter
    private Long lastModifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date lastModifiedDate;

    @Override
    public @NonNull Optional<Long> getCreatedBy() {
        return Optional.ofNullable(createdBy);
    }

    @Override
    public @NonNull Optional<LocalDateTime> getCreatedDate() {
        return null == createdDate ? Optional.empty() : Optional.of(LocalDateTime.ofInstant(createdDate.toInstant(), ZoneId.systemDefault()));
    }

    @Override
    public void setCreatedDate(LocalDateTime creationDate) {
        this.createdDate = Date.from(creationDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public @NonNull Optional<Long> getLastModifiedBy() {
        return Optional.ofNullable(lastModifiedBy);
    }

    @Override
    public @NonNull Optional<LocalDateTime> getLastModifiedDate() {
        return null == lastModifiedDate ? Optional.empty() : Optional.of(LocalDateTime.ofInstant(lastModifiedDate.toInstant(), ZoneId.systemDefault()));
    }

    @Override
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = Date.from(lastModifiedDate.atZone(ZoneId.systemDefault()).toInstant());
    }
}
