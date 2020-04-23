package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.user.domain.common.GeneralEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/04/08
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Getter
@NoArgsConstructor
@Description(comment = "用户权限实体")
public class Permission extends GeneralEntity implements Serializable {

    @Id
    @Column(length = 20, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    @Description(comment = "权限名称")
    private String name;

    @Builder(buildMethodName = "done", builderMethodName = "create")
    public Permission(String name) {
        this.name = name;
    }
}
