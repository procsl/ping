package cn.procsl.ping.boot.user.domain.common;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.boot.domain.business.utils.PathUtils;
import com.querydsl.core.types.Predicate;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractTreeService<T extends AdjacencyNode<ID, N>, ID extends Serializable, N extends AdjacencyPathNode<ID>> extends AbstractService<ID, T> {

    final protected AdjacencyTreeRepository<T, ID, N> treeRepository;

    public AbstractTreeService(JpaRepository<T, ID> jpaRepository,
                               QuerydslPredicateExecutor<T> querydslRepository,
                               AdjacencyTreeRepository<T, ID, N> treeRepository
    ) {
        super(jpaRepository, querydslRepository);
        this.treeRepository = treeRepository;
    }

    /**
     * 搜索
     *
     * @param pathNodes  路径
     * @param predicate  搜索条件,可为空
     * @param onSearched 当搜索成功之后的回调
     * @return 返回搜索到的节点
     * @throws IllegalArgumentException 如果传入错误的参数
     */
    @Transactional(readOnly = true)
    public Map<String, T> search(@NotNull List<String> pathNodes, Function<Integer, Predicate> predicate, BiFunction<T, String, T> onSearched) throws IllegalArgumentException {
        if (CollectionUtils.isEmpty(pathNodes)) {
            return Collections.emptyMap();
        }

        HashMap<String, T> map = new HashMap<>(pathNodes.size());
        for (String path : pathNodes) {
            T result = this.searchOne(path, predicate);
            if (onSearched != null) {
                result = onSearched.apply(result, path);
            }
            map.put(path, result);
        }
        return map;
    }

    /**
     * 搜索方法, 通过指定路径搜索, 其他搜索方法全部基于此方法
     *
     * @param path      路径
     * @param predicate 搜索条件
     * @return 当搜索到数据时返回
     * @throws IllegalArgumentException 不合法参数
     */
    public abstract T searchOne(@NotBlank String path, Function<Integer, Predicate> predicate) throws IllegalArgumentException;

    /**
     * id 转换为去重后的实体
     *
     * @param ids       指定的ids
     * @param delimiter 分隔符
     * @return 返回去重后的实体
     */
    public <TYPE> Collection<TYPE> convertTo(Collection<ID> ids,
                                             @NotNull Function<ID, TYPE> convert,
                                             @NotBlank @Size(min = 1, max = 1) String delimiter) {

        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<ID>> map = new HashMap<>(ids.size());
        for (ID id : ids) {
            List<ID> nodes = treeRepository
                .getParents(treeRepository.getQAdjacency().id, id)
                .map(item -> (ID) item)
                .collect(Collectors.toUnmodifiableList());
            String path = PathUtils.standardize(String.join(delimiter, String.valueOf(nodes)), delimiter);
            map.put(path, nodes);
        }
        // 通过路径去重
        List<String> paths = PathUtils.distinct(map.keySet(), delimiter);
        // 取最后一位角色
        return paths.stream()
            .map(map::get)
            .map(item -> item.get(item.size() - 1))
            .map(convert::apply)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * 按节点搜索
     *
     * @param pathNodes  路径节点
     * @param onSearched 当搜索到时
     * @return 返回搜索路径及对应的节点值
     * @throws IllegalArgumentException 参数错误时异常
     */
    public Map<String, T> search(@NotNull List<String> pathNodes, @NonNull BiFunction<T, String, T> onSearched) throws IllegalArgumentException {
        return this.search(pathNodes, null, onSearched);
    }

    /**
     * 按节点搜索
     *
     * @param pathNodes 路径结点
     * @return 返回搜索到的路径对应的数据
     * @throws IllegalArgumentException 如果参数不合法
     */
    @Transactional
    public Map<String, T> search(@NotNull List<String> pathNodes) throws IllegalArgumentException {
        return this.search(pathNodes, null);
    }


    /**
     * 搜索指定的实体
     *
     * @param path 指定路径
     * @return 返回搜索到的路径
     */
    @Transactional(readOnly = true)
    public T searchOne(@NotBlank String path) throws IllegalArgumentException {
        return this.searchOne(path, null);
    }

}
