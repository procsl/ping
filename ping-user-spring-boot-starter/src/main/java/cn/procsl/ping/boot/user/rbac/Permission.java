package cn.procsl.ping.boot.user.rbac;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * 权限值对象
 */
@Data
@Embeddable
public class Permission implements Serializable {
    String name;
}
