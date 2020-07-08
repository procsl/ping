package cn.procsl.ping.boot.user.domain.rbac.entity;

import cn.procsl.ping.boot.user.utils.StringUtils;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author procsl
 * @date 2020/07/08
 */
@ToString
@EqualsAndHashCode(exclude = "name")
@Setter(PRIVATE)
@Getter
@Embeddable
@NoArgsConstructor(access = PRIVATE)
public class Permission implements Serializable {

    private Long resourceId;

    @Enumerated
    @Column(length = 15)
    private Operation operation;

    private String name;

    protected Permission(Long resourceId, Operation operation, String name) {
        this.resourceId = resourceId;
        this.operation = operation;
        this.name = name;
    }

    public static Permission create(@NonNull Long resourceId, @NonNull Operation operation, String name) {
        if (StringUtils.isEmpty(name)) {
            name = operation.getName();
        }
        return new Permission(resourceId, operation, name);
    }
}
