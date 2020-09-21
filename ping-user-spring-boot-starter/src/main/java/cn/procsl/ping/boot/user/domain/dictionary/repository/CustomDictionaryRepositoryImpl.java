package cn.procsl.ping.boot.user.domain.dictionary.repository;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.business.utils.PathUtils;
import cn.procsl.ping.boot.user.domain.dictionary.model.DictPath;
import cn.procsl.ping.boot.user.domain.dictionary.model.Dictionary;
import cn.procsl.ping.business.domain.DomainId;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.procsl.ping.boot.user.domain.dictionary.model.Dictionary.SPACE_NAME_LEN;
import static cn.procsl.ping.boot.user.domain.dictionary.model.Dictionary.delimiter;

@RequiredArgsConstructor
public class CustomDictionaryRepositoryImpl implements CustomDictionaryRepository {

    final AdjacencyTreeRepository<Dictionary, Long, DictPath> adjacencyTreeRepository;

    @Override
    public List<Long> search(@NonNull String path, @NonNull boolean ignoreActive)
        throws IllegalArgumentException {

        List<DictId> ids = this.search(Projections.constructor(DictId.class, D.id), path, index -> ignoreActive ? null : D.state.isTrue());
        return ids.stream().map(id -> id.id).collect(Collectors.toList());
    }

    /**
     * 获取指定path的值
     *
     * @param nodes        以分割符分割的路径 例如: /root/profile/config
     * @param ignoreActive 是否忽略任意节点上的active
     * @return 如果找到, 则返回指定的值的id
     * @throws IllegalArgumentException 如果分割符不合法,则抛出此异常
     */
    @Override
    public List<Long> search(@NonNull List<String> nodes, @NonNull boolean ignoreActive) throws IllegalArgumentException {
        List<DictId> ids = this.search(Projections.constructor(DictId.class, D.id), nodes, index -> ignoreActive ? null : D.state.isTrue());
        return ids.stream().map(id -> id.id).collect(Collectors.toList());
    }

    @Override
    public <T extends DomainId<Long>> List<T> search(@NonNull Expression<T> selector,
                                                     @NonNull List<String> nodes,
                                                     Function<Integer, Predicate> fun) throws IllegalArgumentException {
        for (String s : nodes) {
            if (s.length() > SPACE_NAME_LEN) {
                throw new IllegalArgumentException(s + ":space长度超长");
            }
        }

        return adjacencyTreeRepository.searchAll(
            selector,
            nodes,
            i -> D.depth.eq(i).and(D.space.eq(nodes.get(i)).and(fun.apply(i))),
            false
        );
    }

    /**
     * 最大化搜索路径
     *
     * @param selector 选择器
     * @param path     路径
     * @param fun      条件回调函数
     * @return 如果搜索成功, 返回与nodes下标对应的类型列表
     */
    @Override
    public <T extends DomainId<Long>> List<T> search(@NonNull Expression<T> selector,
                                                     @NonNull String path,
                                                     Function<Integer, Predicate> fun) throws IllegalArgumentException {

        List<String> nodes = PathUtils.split(path, delimiter.get());
        return this.search(selector, nodes, fun);
    }

    @Value
    protected static class DictId implements DomainId<Long> {
        Long id;
    }

}
