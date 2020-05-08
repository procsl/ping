package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.annotation.Description;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

import static cn.procsl.ping.boot.user.domain.common.GeneralEntity.GENERAL_ENTITY_ID_LENGTH;

/**
 * @author procsl
 * @date 2020/04/08
 */
@Entity
@Table
@Getter
@Description(comment = "用户权限实体")
public class Permission implements Serializable {

    public final static String PERMISSION_ID_NAME = "permission_id";

    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Description(comment = "对应的的资源ID")
    @Column(name = "resource_id", length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    protected Long resource;


    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {PERMISSION_ID_NAME, "operation"}))
    @ElementCollection
    @Column(name = "operation", updatable = false, length = 20)
    @Description(comment = "支持的操作, 针对当前关联的资源")
    protected Set<String> operations;

    @Builder(buildMethodName = "done", builderMethodName = "create")
    public Permission() {
    }
}
