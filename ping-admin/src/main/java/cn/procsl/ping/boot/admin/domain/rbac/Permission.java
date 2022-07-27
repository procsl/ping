package cn.procsl.ping.boot.admin.domain.rbac;

import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
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
        return String.format("[%s: %s]", getOperate(), getResource());
    }

    public abstract String getOperate();

    public abstract void setOperate(String option);

    public abstract String getResource();

    public abstract void setResource(String resource);

    public boolean matcher(String option, String resource) {
        return ObjectUtils.nullSafeEquals(option, this.getOperate()) && ObjectUtils.nullSafeEquals(resource,
                this.getResource());
    }

    public boolean matcher(Function<Permission, Boolean> function) {
        return function.apply(this);
    }

    public void update(String option, String resource) {
        this.setOperate(option);
        this.setResource(resource);
    }

    @Transient
    public String getType() {
        val discriminatorValues = this.getClass().getAnnotationsByType(DiscriminatorValue.class);
        if (discriminatorValues.length == 0) {
            return null;
        }
        return discriminatorValues[0].value();
    }

}

