package cn.procsl.ping.boot.domain.business.tree.repository;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

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


    <Projection> Projection findOne(@NonNull Expression<Projection> select, Predicate... predicates);

    <Projection> Stream<Projection> findAll(@NonNull Expression<Projection> select, Predicate... predicates);

    <Projection> Page<Projection> findAll(@NonNull Expression<Projection> select,
                                          @NonNull Pageable pageable, Predicate... predicates);

    /**
     * 查询父节点列表, 通过指定的id
     *
     * @param select       字段信息
     * @param id           指定的id
     * @param predicates   查询条件
     * @param <Projection> 投影
     * @return 返回投影类型的Stream
     */
    <Projection> Stream<Projection> getParents(@NonNull Expression<Projection> select,
                                               @NonNull ID id, Predicate... predicates);

    /**
     * 分页查询
     *
     * @param select       指定查询字段
     * @param pageable     分页条件
     * @param id           指定的id
     * @param predicates   自定义条件
     * @param <Projection> 投影类
     * @return 返回分页的投影信息
     */
    <Projection> Page<Projection> getParents(@NonNull Expression<Projection> select,
                                             Pageable pageable,
                                             @NonNull ID id, Predicate... predicates);

    /**
     * 查询指定节点的所有直接子节点
     *
     * @param select     选择器
     * @param id         指定的id
     * @param predicates 查询条件
     * @return 返回子节点stream
     */
    <Projection> Stream<Projection> getDirectChildren(@NonNull Expression<Projection> select,
                                                      @NonNull ID id, Predicate... predicates);


    /**
     * 获取指定节点的所有子节点
     *
     * @param select       选择器
     * @param id           指定节点id
     * @param predicates   条件
     * @param <Projection> 投影类型
     * @return 返回指定投影类型的Stream
     */
    <Projection> Stream<Projection> getAllChildren(@NonNull Expression<Projection> select,
                                                   @NonNull ID id, Predicate... predicates);

    /**
     * 分页查询指定节点的所有子节点
     *
     * @param pageable     分页条件
     * @param select       选择器
     * @param id           指定节点id
     * @param predicates   条件
     * @param <Projection> 投影类型
     * @return 返回分页投影
     */
    <Projection> Page<Projection> getAllChildren(@NonNull Expression<Projection> select,
                                                 @NonNull Pageable pageable,
                                                 ID id, Predicate... predicates);

    /**
     * 获取直接父节点
     *
     * @param select 选择器
     * @param id     指定的节点ID
     * @return 返回直接父节点
     */
    <Projection> Optional<Projection> getDirectParent(@NonNull Expression<Projection> select, @NonNull ID id);

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
     * @param start        头节点
     * @param end          尾节点
     * @param select       选择器,选择返回指定的值
     * @param predicates   过滤条件, 可以过滤指定的条件
     * @param <Projection> 投影信息
     * @return 如果两个节点为父子关系, 则返回他们的关系链路列表
     */
    <Projection> Stream<Projection> findLinks(@NonNull Expression<Projection> select,
                                              @NonNull ID start, @NonNull ID end, Predicate... predicates);

    /**
     * 分页查询指定的链路
     *
     * @param select       选择器
     * @param pageable     分页
     * @param start        起始节点
     * @param end          结束节点
     * @param predicates   条件
     * @param <Projection> 投影
     * @return 返回分页投影
     */
    <Projection> Page<Projection> findLinks(@NonNull Expression<Projection> select,
                                            @NonNull Pageable pageable,
                                            ID start,
                                            ID end, Predicate... predicates);

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
     * 计算两个节点中谁为子节点, 并且返回子节点信息
     *
     * @param select       选择的节点字段
     * @param pre          节点1
     * @param next         节点2
     * @param <Projection> 投影信息
     * @return 返回指定的投影的信息, 如果不存在关系, 则返回null
     */
    <Projection> Projection calcChildren(@NonNull Expression<Projection> select,
                                         @NonNull ID pre, @NonNull ID next);

    /**
     * 计算父节点
     *
     * @param select       选择的节点字段
     * @param pre          节点1
     * @param next         节点2
     * @param <Projection> 投影信息
     * @return 返回指定的投影的信息, 如果不存在, 则返回null
     */
    <Projection> Projection calcParent(@NonNull Expression<Projection> select,
                                       @NonNull ID pre, @NonNull ID next);

    /**
     * 获取根节点Stream
     *
     * @param select       用于指定返回的值
     * @param predicates   查询条件
     * @param <Projection> 投影
     * @return 返回指定值的投影Stream
     */
    <Projection> Stream<Projection> getRoots(@NonNull Expression<Projection> select,
                                             Predicate... predicates);

    /**
     * @param select       指定返回的值
     * @param pageable     分页条件
     * @param predicates   条件
     * @param <Projection> 投影
     * @return 返回指定值的投影
     */
    <Projection> Page<Projection> getRoots(@NonNull Expression<Projection> select,
                                           @NonNull Pageable pageable,
                                           Predicate... predicates);

}
