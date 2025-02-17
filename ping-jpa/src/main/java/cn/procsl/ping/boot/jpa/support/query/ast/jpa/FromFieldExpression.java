package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.Join;
import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.ast.*;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
final class FromFieldExpression implements FromExpression {

    private final Projection projection;
    private final List<Join> joinFields;
    private final Class<?> projectionType;

    public Class<?> getEntityType() {
        return this.projection.entity();
    }

    @Override
    public Expression getExpression() {
        return new StringExpression(projection.entity().getName());
    }

    @Override
    public String getTableAlias() {
        return projection.alias();
    }

    @Override
    public List<JoinExpression> getJoins() {
        if (joinFields == null || joinFields.isEmpty()) {
            return Collections.emptyList();
        }

        return this.joinFields.stream()
            .filter(item -> ObjectUtils.nullSafeEquals(this.projection.alias(), item.ref()))
            .map(item -> {
                Projection proj = item.join();
                final FromFieldExpression joinItem = new FromFieldExpression(proj, this.joinFields, this.projectionType);
                return new InnerJoinExpression(joinItem, item);
            }).collect(Collectors.toList());
    }

    @RequiredArgsConstructor
    static class InnerJoinExpression implements JoinExpression {

        private final FromFieldExpression from;
        private final Join joinField;

        @Override
        public Expression getExpression() {
            return this.from.getExpression();
        }

        @Override
        public String getTableAlias() {
            return this.from.getTableAlias();
        }

        @Override
        public List<JoinExpression> getJoins() {
            return this.from.getJoins();
        }

        @Override
        public JoinType getJoinType() {
            return joinField.type();
        }

        @Override
        public String getLeftColumnName() {
            return joinField.leftJoinField();
        }

        @Override
        public String getRightColumnName() {
            return joinField.rightJoinField();
        }
    }


    @Override
    public String toString() {
        return this.toExpString();
    }

}
