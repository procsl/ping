package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.JoinField;
import cn.procsl.ping.boot.jpa.support.query.Projection;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.*;

final class FromClauseBuilder implements QueryBuilder {

    @Override
    public Optional<List<? extends Clause>> parse(@NonNull QueryContext context) {

        Class<?> query = context.getQueryClass();
        Projection projection = AnnotationUtils.findAnnotation(query, Projection.class);
        if (projection == null) {
            throw new IllegalArgumentException(query + "未标注 @Projection 注解");
        }

        List<JoinField> joinFields = new ArrayList<>();
        {
            JoinField.Joins joins = AnnotationUtils.findAnnotation(query, JoinField.Joins.class);
            if (joins != null) {
                joinFields.addAll(Arrays.asList(joins.value()));
            } else {
                JoinField joinField = AnnotationUtils.findAnnotation(query, JoinField.class);
                joinFields.add(joinField);
            }
        }

        SimpleFromClause rootClause = new SimpleFromClause(new FragmentClause(projection.entity().getName()), projection.alias());
        // simple
        if (joinFields.isEmpty()) {
            return Optional.of(Collections.singletonList(rootClause));
        }

        HashMap<String, ComposeJoinClause> map = new HashMap<>();
        map.put(projection.alias(), new ComposeJoinClause(rootClause));
        // 创建所有的join对象
        for (JoinField field : joinFields) {
            Projection joinProjection = field.to();
            SimpleFromClause joinClause = new SimpleFromClause(new FragmentClause(joinProjection.entity().getName()), joinProjection.alias());
            map.put(joinProjection.alias(), new ComposeJoinClause(joinClause));
        }
        // 建立join关系
        for (JoinField field : joinFields) {
            String alias = field.fromAlias();
            ComposeJoinClause compose = map.get(alias);
            if (compose == null) {
                throw new IllegalArgumentException(alias + "对应的join不存在");
            }
            Projection joinProjection = field.to();
            SimpleFromClause joinClause = new SimpleFromClause(new FragmentClause(joinProjection.entity().getName()), joinProjection.alias());
            compose.joinTo(field.joinType(), joinClause, field.leftJoinField(), field.rightJoinField());
        }

        return Optional.empty();
    }


}
