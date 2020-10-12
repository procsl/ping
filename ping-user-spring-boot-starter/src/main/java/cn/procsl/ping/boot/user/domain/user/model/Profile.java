package cn.procsl.ping.boot.user.domain.user.model;

import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * 用户配置
 */
@Embeddable
@NoArgsConstructor
public class Profile implements Serializable {

    public final static int REAL_NAME_LEN = 20;

    @Column(length = REAL_NAME_LEN)
    String name;

    public Profile(String name) {
        this.name = name;
    }
}
