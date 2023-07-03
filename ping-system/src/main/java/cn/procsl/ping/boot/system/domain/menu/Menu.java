package cn.procsl.ping.boot.system.domain.menu;

import cn.procsl.ping.boot.jpa.domain.tree.AdjacencyNode;
import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@RepositoryCreator
@Table(name = "s_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu implements AdjacencyNode<Long, MenuNode> {

    @Id
    @GeneratedValue
    Long id;

    @Column(nullable = false, length = 40)
    String name;

    @Column(nullable = false)
    Long parentId;

    @Column(nullable = false)
    Integer depth;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    Set<MenuNode> path;

    @Override
    public MenuNode createPathNodeByCurrent() {
        return new MenuNode(id, parentId);
    }

    @Override
    public void changeParent(@NonNull AdjacencyNode<Long, MenuNode> parent) {
        cleanPath();
        this.setParentId(parent.getId());
        this.setDepth(parent.getDepth() + 1);
        this.addPathNodes(parent.getPath());
        this.addPathNode(this.createPathNodeByCurrent());
    }

    @Override
    public void addPathNodes(Collection<MenuNode> nodes) {
        initPathCollection();
        this.path.addAll(nodes);
    }

    @Override
    public void addPathNode(MenuNode node) {
        initPathCollection();
        this.path.add(node);
    }


    void cleanPath() {
        initPathCollection();
        path.clear();
    }

    void initPathCollection() {
        if (this.path == null) {
            this.path = new HashSet<>();
        }
    }


}
