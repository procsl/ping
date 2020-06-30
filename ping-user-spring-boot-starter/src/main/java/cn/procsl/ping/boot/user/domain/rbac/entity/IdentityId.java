package cn.procsl.ping.boot.user.domain.rbac.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author procsl
 * @date 2020/06/24
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdentityId {
    private Long id;

    public IdentityId(@NonNull Long id) {
        this.id = id;
    }
}
