package cn.procsl.ping.boot.system.domain.rbac;

import cn.procsl.ping.boot.common.jpa.DiscriminatorValueFinder;
import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import lombok.*;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.*;
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
public abstract class Permission implements Serializable, DiscriminatorValueFinder {

    @Id
    @GeneratedValue
    Long id;

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
        return this.find();
    }

}

