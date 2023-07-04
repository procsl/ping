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

    @Transient
    boolean isNewInstance = false;

    @Id
    @Access(AccessType.PROPERTY)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;

    @Column(nullable = false, length = 40)
    String name;

    @Column(nullable = false, length = 150)
    String router;

    @Column(nullable = false)
    Long parentId;

    @Column(nullable = false)
    Integer depth;

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"), name = "s_menu_path")
    Set<MenuNode> path;

    @Override
    public MenuNode createPathNodeByCurrent() {
        return new MenuNode(id, parentId);
    }

    protected Menu(String name, Long parentId, String router, Integer depth, Set<MenuNode> path) {
        this.isNewInstance = true;
        this.name = name;
        this.parentId = parentId;
        this.depth = depth;
        this.router = router;
        if (!path.isEmpty()) {
            this.path = new HashSet<>(path);
        }
    }

    public void setId(Long id) {
        this.id = id;
        if (isNewInstance) {
            this.addPathNode(this.createPathNodeByCurrent());
            this.isNewInstance = false;
        }
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
    public void addPathNodes(@NonNull Collection<MenuNode> nodes) {
        initPathCollection();
        this.path.addAll(nodes);
    }

    @Override
    public void addPathNode(@NonNull MenuNode node) {
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

    public static Menu createRoot(String name, String router) {
        return new Menu(name, SUPER_ROOT_ID, router, ROOT_DEPTH, new HashSet<>());
    }

    public Menu createChild(String name, String router) {
        if (this.isNewInstance) {
            throw new IllegalArgumentException("需要先持久化生成ID");
        }
        return new Menu(name, this.getId(), router, this.getDepth() + 1, this.getPath());
    }

    @Override
    public String toString() {
        return "{id=" + id + ", name='" + name + '\'' + ", parentId=" + parentId + ", depth=" + depth + '}';
    }
}
