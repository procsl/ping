package cn.procsl.ping.boot.user.domain.resource.entity;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.data.business.entity.GeneralEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PRIVATE;

/**
 * @author procsl
 * @date 2020/05/09
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Getter
@NoArgsConstructor
@Setter(PRIVATE)
@Description(comment = "资源聚合")
@CreateRepository
public class Resource extends GeneralEntity {

    protected final static String RESOURCE_ID_NAME = "resource_id";

    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Description(comment = "资源名称")
    @Column(length = 20)
    protected String name;

    @Embedded
    @Basic(fetch = FetchType.LAZY)
    protected ResourceTreeNode node;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {RESOURCE_ID_NAME, "depend_id"}))
    @ElementCollection
    @Column(name = "depend_id", nullable = false, updatable = false, length = GENERAL_ENTITY_ID_LENGTH)
    @Description(comment = "依赖的资源")
    protected Set<Long> depends;

    @Description(comment = "资源类型")
    @Enumerated(STRING)
    @Column(length = 15)
    protected ResourceType type;
}