package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.domain.business.utils.ObjectUtils;
import lombok.NonNull;
import lombok.Value;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

import static cn.procsl.ping.boot.user.domain.rbac.model.Permission.OPS_LEN;
import static cn.procsl.ping.boot.user.domain.rbac.model.Permission.RES_NO_LEN;

@MappedSuperclass
public interface Target extends Serializable {

    static SimpleTarget toTarget(
        @NonNull String resource,
        @NonNull String operator
    ) {
        return new SimpleTarget(resource, operator);
    }

    static <T extends Target> SimpleTarget toSimpleTarget(@NonNull T target) {
        if (target instanceof SimpleTarget) {
            return (SimpleTarget) target;
        }
        return new SimpleTarget(target.getResource(), target.getOperator());
    }

    @NotNull
    @Size(min = 1, max = RES_NO_LEN)
    String getResource();

    @NotNull
    @Size(min = 1, max = OPS_LEN)
    String getOperator();

    /**
     * 判断两个target是否为业务意义上的相等
     *
     * @param target
     * @param <T>
     * @return
     */
    default <T extends Target> boolean isEquals(T target) {
        boolean bool =
            ObjectUtils.nullSafeEquals(target.getResource(), this.getResource())
                && ObjectUtils.nullSafeEquals(target.getOperator(), this.getOperator());
        return bool;
    }

    @Value
    final class SimpleTarget implements Target {
        @NotNull
        @Size(min = 1, max = OPS_LEN)
        final String resource;

        @NotNull
        @Size(min = 1, max = OPS_LEN)
        final String operator;
    }

}
