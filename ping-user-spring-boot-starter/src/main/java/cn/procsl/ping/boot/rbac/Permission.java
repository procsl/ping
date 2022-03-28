package cn.procsl.ping.boot.rbac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * 权限值对象
 */
@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Permission implements Serializable {

    /**
     * 权限名称
     */
    String name;
}
