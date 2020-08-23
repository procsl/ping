package cn.procsl.ping.boot.domain.business.dictionary.model;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 数据字典
 *
 * @author procsl
 * @date 2020年8月23日
 */
@Data
@Table
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)// for jpa
@Slf4j
public class DataDictionary implements AdjacencyNode<Long, DictionaryPathNode> {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "general")
    @SequenceGenerator(allocationSize = 100, name = "general")
    protected Long id;

    @Column(length = 20, nullable = false)
    protected String space;

    @Column(nullable = false)
    protected Long parentId;

    @Column(nullable = false)
    protected Integer depth;

    @Transient
    public static String delimiter = "/";

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    protected Set<DictionaryPathNode> path;


    @Override
    public DictionaryPathNode currentPathNode() {
        return new DictionaryPathNode(this.getId(), this.getSpace(), this.getDepth());
    }

    /**
     * 修改父节点
     *
     * @param parent 指定的父节点
     */
    @Override
    public void changeParent(AdjacencyNode<Long, DictionaryPathNode> parent) {
        log.info("修改当前节点的父节点");
        if (!(parent instanceof DataDictionary)) {
            throw new IllegalArgumentException("required " + this.getClass().getName());
        }
        this.empty();
        this.setParentId(parent.getId());
        log.trace("修改parentId:{}", this.getParentId());

        this.setDepth(((DataDictionary) parent).getDepth() + 1);
        log.trace("修改depth:{}", this.getDepth());

        this.getPath().addAll(parent.getPath());
        log.trace("添加父节点的path:{}", this.getPath());

        this.getPath().add(parent.currentPathNode());
        this.upgrade();
    }

    /**
     * 对于长文本可以使用以下输出流的方法读取
     *
     * @param outputStream
     */
    public void format(OutputStream outputStream) throws IOException {
        // TODO
    }

    /**
     * 格式化输出当前的名称的路径 用于展示
     *
     * @return if id !== null return path string
     */
    public String format() {
        // if new
        if (this.getId() == null) {
            return null;
        }

        List<String> spaceList = this.getPath()
                .stream()
                .sorted((pre, next) -> pre.getSeq() - next.getSeq())
                .map(item -> item.getSpace())
                .collect(Collectors.toList());

        String tmp = String.join(delimiter, spaceList);
        return delimiter.concat(tmp);
    }

    void upgrade() {

        log.trace("开始更新自身");
        if (this.getId() == null) {
            log.trace("当前ID不存在, 放弃更新自身");
            return;
        }

        // 添加当前节点
        if (this.getParentId() == null) {
            log.trace("设置当前的id为parentId:{}", id);
            this.setParentId(id);
        }

        this.getPath()
                .add(this.currentPathNode());
    }

    void empty() {
        // 如果为root则parentId == id
        log.trace("当前实体被置空");
        log.trace("设置parent=id:{}", id);
        this.setParentId(id);
        log.trace("设置depth:{}", id);
        this.setDepth(0);
        if (this.getPath() == null) {
            log.trace("初始化一个空的path");
            this.setPath(new HashSet<>());
        } else {
            log.trace("清空path");
            this.getPath().clear();
        }
    }

}
