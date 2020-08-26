package cn.procsl.ping.boot.domain.business.dictionary.repository;

import cn.procsl.ping.boot.domain.business.dictionary.model.Dictionary;
import cn.procsl.ping.business.domain.DomainId;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@RequiredArgsConstructor
public class CustomDictionaryRepositoryImpl implements CustomDictionaryRepository {

    final JPAQueryFactory jpaQueryFactory;

    @Override
    public <T extends DomainId<Long>> T search(@NotNull String path,
                                               @NotNull boolean ignoreActive, @NotNull Expression<T> select)
        throws IllegalArgumentException {

        List<String> nodes = Dictionary.split(path);
        Long parentId = null;
        for (int i = 0; i < nodes.size(); i++) {
            // depth ==:depth and space == :space
            BooleanExpression where = dict.depth.eq(i).and(dict.space.eq(nodes.get(i)));
            if (!ignoreActive) {
                // active is true
                where.and(dict.active.isTrue());
            }
            // parentId == id ; parentId == :parentId
            BooleanExpression exp = parentId == null ? dict.parentId.eq(dict.id) : dict.parentId.eq(parentId);
            where.and(exp);
            // 最后一个
            if (i + 1 >= nodes.size()) {
                return this.jpaQueryFactory.select(select).from(dict).where(where).fetchOne();
            }

            // 如果不是最后一个, 继续使用条件查找, 如果没有找到, 那说明不存在这个节点, 直接返回
            Long id = this.jpaQueryFactory.select(dict.id).from(dict).where(where).fetchOne();
            if (id == null) {
                return null;
            }
            parentId = id;
        }
        return null;
    }
}
