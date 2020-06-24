package cn.procsl.ping.boot.user.domain.rbac.entity;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.annotation.Description;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

import static cn.procsl.ping.boot.data.business.entity.GeneralEntity.GENERAL_ENTITY_ID_LENGTH;
import static lombok.AccessLevel.PRIVATE;

/**
 * 用户权限实体
 * @author procsl
 * @date 2020/04/08
 */
@Entity
@Table
@Getter
@Setter(PRIVATE)
@Description(comment = "用户权限实体")
@CreateRepository
public class Permission implements Serializable {

    public final static String PERMISSION_ID_NAME = "permission_id";

    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Description(comment = "对应的的资源ID")
    protected Long resourceId;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {PERMISSION_ID_NAME, "operation"}))
    @ElementCollection
    @Column(name = "operation", updatable = false, length = 20)
    @Description(comment = "支持的操作, 针对当前关联的资源")
    protected Set<String> operations;

//    @Builder(buildMethodName = "done", builderMethodName = "create")
//    public Permission() {
//    }

}
