package cn.procsl.ping.boot.infra.domain.rbac;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.function.Function;

/**
 * 权限值对象
 */
@Setter
@Getter
@Entity
@RepositoryCreator
@Table(name = "i_permission")
@DiscriminatorColumn(name = "type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Permission extends AbstractPersistable<Long> implements Serializable {

    @Override
    public @NonNull String toString() {
        return String.format("[%s: %s]", getOption(), getResource());
    }

    public abstract String getOption();

    public abstract void setOption(String option);

    public abstract String getResource();

    public abstract void setResource(String resource);

    public boolean matcher(String option, String resource) {
        return ObjectUtils.nullSafeEquals(option, this.getOption()) && ObjectUtils.nullSafeEquals(resource, this.getResource());
    }

    public boolean matcher(Function<Permission, Boolean> function) {
        return function.apply(this);
    }

    public void update(String option, String resource) {
        this.setOption(option);
        this.setResource(resource);
    }
}

