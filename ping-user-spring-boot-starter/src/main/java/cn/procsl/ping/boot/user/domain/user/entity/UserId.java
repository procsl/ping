package cn.procsl.ping.boot.user.domain.user.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

/**
 * 用户ID
 *
 * @author procsl
 * @date 2020/07/07
 */
@Data
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, staticName = "of")
public final class UserId {
    private Long id;
}
