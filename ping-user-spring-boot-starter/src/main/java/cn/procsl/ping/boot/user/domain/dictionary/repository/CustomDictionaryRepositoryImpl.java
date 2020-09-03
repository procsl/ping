package cn.procsl.ping.boot.user.domain.dictionary.repository;

import cn.procsl.ping.boot.user.domain.dictionary.model.Dictionary;
import cn.procsl.ping.business.domain.DomainId;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.procsl.ping.boot.user.domain.dictionary.model.Dictionary.SPACE_NAME_LEN;

@RequiredArgsConstructor
public class CustomDictionaryRepositoryImpl implements CustomDictionaryRepository {

    final JPAQueryFactory jpaQueryFactory;

    final Function<Integer, Predicate> function = index -> null;

    @Override
    public List<Long> search(@NonNull String path, @NonNull boolean ignoreActive)
        throws IllegalArgumentException {

        List<DictId> ids = this.search(Projections.constructor(DictId.class, D.id), path, index -> ignoreActive ? null : D.active.isTrue());
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
        List<DictId> ids = this.search(Projections.constructor(DictId.class, D.id), nodes, index -> ignoreActive ? null : D.active.isTrue());
        return ids.stream().map(id -> id.id).collect(Collectors.toList());
    }

    @Override
    public <T extends DomainId<Long>> List<T> search(@NonNull Expression<T> selector,
                                                     @NonNull List<String> nodes,
                                                     Function<Integer, Predicate> fun) throws IllegalArgumentException {
        if (nodes.isEmpty()) {
            return Collections.emptyList();
        }

        for (String s : nodes) {
            if (s.length() > SPACE_NAME_LEN) {
                throw new IllegalArgumentException(s + ":space长度超长");
            }
        }

        if (fun == null) {
            fun = function;
        }

        ArrayList<T> list = new ArrayList<>(nodes.size());
        T parent = null;
        for (int i = 0; i < nodes.size(); i++) {
            //depth ==:depth and space == :space
            BooleanExpression where = D.depth.eq(i).and(D.space.eq(nodes.get(i)));

            // parentId == id ; parentId == :parentId
            BooleanExpression exp = parent == null ? D.parentId.eq(D.id) : D.parentId.eq(parent.getId());
            where.and(exp);
            Predicate tp = fun.apply(i);
            if (tp != null) {
                where = where.and(tp);
            }

            T projection = this.jpaQueryFactory.select(selector).from(D).where(where).fetchOne();
            if (projection == null) {
                break;
            }
            list.add(i, projection);
        }
        return list;
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

        List<String> nodes = Dictionary.split(path);
        return this.search(selector, nodes, fun);
    }

    @Value
    protected static class DictId implements DomainId<Long> {
        private final Long id;
    }

}
