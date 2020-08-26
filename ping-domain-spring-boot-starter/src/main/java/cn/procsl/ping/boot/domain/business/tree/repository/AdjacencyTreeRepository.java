package cn.procsl.ping.boot.domain.business.tree.repository;

import cn.procsl.ping.boot.domain.business.common.model.Operator;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 邻接树
 *
 * @author procsl
 * @date 2020/07/31
 */
@NoRepositoryBean
public interface AdjacencyTreeRepository<
        T extends AdjacencyNode<ID, P>,
        ID extends Serializable,
        P extends AdjacencyPathNode<ID>> extends Repository<T, ID> {

    /**
     * 查询父节点列表, 通过指定的id
     *
     * @param id 指定的ID
     * @return 返回父节点Stream
     */
    Stream<T> getParents(ID id);

    /**
     * 获取指定节点的所有父节点IDs
     *
     * @param id 指定的ID
     * @return 返回父节点Stream
     */
    Stream<ID> getParentIds(ID id);

    /**
     * 查询指定节点的所有直接子节点
     *
     * @param id 指定的id
     * @return 返回子节点stream
     */
    Stream<T> getDirectChildren(ID id);

    /**
     * 获取指定子节点的IDs
     *
     * @param id 指定的节点ID
     * @return 返回子节点IDs
     */
    Stream<ID> getDirectChildrenIds(ID id);


    /**
     * 查询所有的子节点, 包含自身节点ID
     *
     * @param id 指定的节点ID
     * @return 返回所有的子节点ID
     */
    Stream<ID> getAllChildrenIds(ID id);

    /**
     * 获取所有的子节点包含自身节点
     *
     * @param id 指定的节点
     * @return 返回所有的子节点
     */
    Stream<T> getAllChildren(ID id);

    /**
     * 获取直接父节点
     *
     * @param id 指定的节点ID
     * @return 返回直接父节点
     */
    Optional<T> getDirectParent(ID id);

    /**
     * 树的深度
     *
     * @param id 指定的节点ID
     * @return 如果不存在, 则返回-1
     */
    int getDepth(ID id);

    /**
     * 指定树节点的最大深度
     *
     * @param id 指定的树节点ID
     * @return 返回指定树的最大深度
     */
    int findMaxDepth(ID id);

    /**
     * 查找指定树等于指定深度节点(含多个)
     *
     * @param id        自定的树ID
     * @param depth     深度条件
     * @param operator  操作符
     * @param direction 排序条件
     * @return 指定深度的列表
     */
    Stream<T> findDepthNodes(ID id, @Nullable Integer depth, Operator operator, Sort.Direction direction);

    /**
     * 移动指定的树节点至目标节点下(即子节点)
     *
     * @param root   源节点
     * @param target 目标节点
     */
    void mount(ID root, ID target);

    /**
     * 删除指定的节点, 包括子节点
     *
     * @param id 指定的节点
     * @return 返回删除个数
     */
    int remove(ID id);

    /**
     * 查询链路节点
     * 从小到大排序
     *
     * @param start 头节点
     * @param end   尾节点
     * @return 如果两个节点为父子关系, 则返回他们的关系链路列表
     */
    Stream<T> findLinks(ID start, ID end);

    /**
     * 获取链路ID
     *
     * @param start 起始节点
     * @param end   尾节点
     * @return 如果两个节点为父子关系, 则返回他们的关系链路列表id
     */
    Stream<ID> findLinkIds(ID start, ID end);

    /**
     * 计算两个节点之间的关系, 返回深度差
     * 如果source 为 target的父节点 则返回 负数, 父节点的深度永远小于子节点的高度
     * 如果source为target的子节点, 则返回正数
     *
     * @param source 节点1
     * @param target 节点2
     * @return 返回(source - target)的高度差, 如果不存在关系返回null, 自身则返回0
     */
    Integer calcDepth(ID source, ID target);

    /**
     * 计算两个节点中谁为子节点, 并且返回子节点ID
     *
     * @param pre  节点1
     * @param next 节点2
     * @return 返回子节点ID
     */
    ID calcChildId(ID pre, ID next);

    /**
     * 获取root节点列表
     *
     * @return root id
     */
    Stream<T> getRoots();

    /**
     * 获取root ids
     *
     * @return root 节点id
     */
    Stream<ID> getRootIds();
}
