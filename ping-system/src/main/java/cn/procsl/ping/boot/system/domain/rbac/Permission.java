package cn.procsl.ping.boot.system.domain.rbac;

import cn.procsl.ping.boot.jpa.support.DiscriminatorValueFinder;
import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 权限值对象
 */
@Setter
@Getter
@Entity
@RepositoryCreator
@Table(name = "s_permission")
@DiscriminatorColumn(name = "type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Permission implements Serializable, DiscriminatorValueFinder {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
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

