package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.JoinField;
import cn.procsl.ping.boot.jpa.support.query.Projection;
import jakarta.persistence.criteria.JoinType;
import lombok.*;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.*;
import java.util.stream.Collectors;

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

        // 创建所有的join对象
        Map<String, List<JoinField>> group = joinFields.stream().collect(Collectors.groupingBy(JoinField::ref));
        FromClause newFrom = this.build(rootClause, group);
        return Optional.of(Collections.singletonList(newFrom));
    }

    private FromClause build(FromClause rootClause, Map<String, List<JoinField>> joinFields) {
        String alias = rootClause.getTableAlias();
        List<JoinField> fields = joinFields.get(alias);
        if (fields == null) {
            return rootClause;
        }
        FromClause from = rootClause;
        for (JoinField field : fields) {
            Projection joinProjection = field.join();
            FragmentClause clause = new FragmentClause(joinProjection.entity().getName());
            SimpleFromClause joinClause = new SimpleFromClause(clause, joinProjection.alias());
            from = SimpleJoinClause.builder()
                .joinType(field.joinType())
                .leftFromClause(from)
                .rightFromClause(joinClause)
                .leftFieldNameAlias(field.leftJoinField())
                .rightFieldNameAlias(field.rightJoinField())
                .build();
        }
        return this.build(from, joinFields);
    }


    @Getter
    static final class SimpleJoinClause implements FromClause {

        private final FromClause leftFromClause;

        private final SimpleFromClause rightFromClause;

        private final String leftFieldNameAlias;

        private final String rightFieldNameAlias;

        private final JoinType joinType;

        @Builder
        private SimpleJoinClause(@NonNull FromClause leftFromClause,
                                 @NonNull SimpleFromClause rightFromClause,
                                 @NonNull String leftFieldNameAlias,
                                 @NonNull String rightFieldNameAlias,
                                 JoinType joinType) {
            this.leftFromClause = leftFromClause;
            this.rightFromClause = rightFromClause;
            this.leftFieldNameAlias = leftFieldNameAlias;
            this.rightFieldNameAlias = rightFieldNameAlias;
            if (joinType == null) {
                this.joinType = JoinType.INNER;
            } else {
                this.joinType = joinType;
            }
        }

        // FROM table_a AS a
        //       INNER JOIN table_b as b on a.id = b.id
        //       INNER JOIN table_c as c on a.id = c.id
        //            INNER JOIN table_d as d on c.id = d.id
        @Override
        public String toClauseString() {
            String main = this.leftFromClause.toClauseString();
            String right = this.rightFromClause.toClauseString();
            return "%s \n\t%s join %s on %s.%s = %s.%s".formatted(main,
                joinType.toString().toLowerCase(), right,
                this.leftFromClause.getTableAlias(),
                this.leftFieldNameAlias,
                this.rightFromClause.getTableAlias(),
                this.rightFieldNameAlias);
        }

        @Override
        public String toString() {
            return this.toClauseString();
        }

        @Override
        public String getTableAlias() {
            return rightFromClause.getTableAlias();
        }
    }

    @Getter
    @RequiredArgsConstructor
    static final class SimpleFromClause implements FromClause {

        private final Clause subClause;

        private final String tableAlias;

        @Setter
        int order;

        @Override
        public String toClauseString() {
            return "%s as %s".formatted(subClause.toClauseString(), tableAlias);
        }

        @Override
        public String toString() {
            return this.toClauseString();
        }

    }

}
