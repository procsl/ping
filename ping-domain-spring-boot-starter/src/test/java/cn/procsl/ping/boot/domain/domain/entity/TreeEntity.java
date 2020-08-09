package cn.procsl.ping.boot.domain.domain.entity;

import cn.procsl.ping.boot.domain.business.entity.AdjacencyNode;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

/**
 * @author procsl
 * @date 2020/07/31
 */
@Data
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class TreeEntity implements AdjacencyNode<String> {

    @Id
    @Column(length = UUID_LENGTH)
    @GeneratedValue(generator = "idGenerator")
    @GenericGenerator(name = "idGenerator", strategy = "uuid2")
    String id;

    @Column(length = UUID_LENGTH)
    String parentId;

    String name;

    Integer depth;

    @ElementCollection
    Set<PathNode> path;

    @Override
    public Set<PathNode> getPath() {
        return path;
    }
}
