package cn.procsl.ping.boot.user.config;

import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Auditable;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@MappedSuperclass
public class AbstractAuditable<U, PK extends Serializable> extends AbstractPersistable<PK> implements Auditable<U, PK, LocalDateTime> {

    @CreatedBy
    @Setter
    private U createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;

    @LastModifiedBy
    @Setter
    private U lastModifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date lastModifiedDate;

    /**
     * Returns the user who created this entity.
     *
     * @return the createdBy
     */
    @Override
    public Optional<U> getCreatedBy() {
        return Optional.ofNullable(createdBy);
    }

    /**
     * Returns the creation date of the entity.
     *
     * @return the createdDate
     */
    @Override
    public Optional<LocalDateTime> getCreatedDate() {
        return null == createdDate ? Optional.empty()
            : Optional.of(LocalDateTime.ofInstant(createdDate.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * Sets the creation date of the entity.
     *
     * @param creationDate the creation date to set
     */
    @Override
    public void setCreatedDate(LocalDateTime creationDate) {
        this.createdDate = Date.from(creationDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Returns the user who modified the entity lastly.
     *
     * @return the lastModifiedBy
     */
    @Override
    public Optional<U> getLastModifiedBy() {
        return Optional.ofNullable(lastModifiedBy);
    }

    /**
     * Returns the date of the last modification.
     *
     * @return the lastModifiedDate
     */
    @Override
    public Optional<LocalDateTime> getLastModifiedDate() {
        return null == lastModifiedDate ? Optional.empty()
            : Optional.of(LocalDateTime.ofInstant(lastModifiedDate.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * Sets the date of the last modification.
     *
     * @param lastModifiedDate the date of the last modification to set
     */
    @Override
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = Date.from(lastModifiedDate.atZone(ZoneId.systemDefault()).toInstant());
    }
}
