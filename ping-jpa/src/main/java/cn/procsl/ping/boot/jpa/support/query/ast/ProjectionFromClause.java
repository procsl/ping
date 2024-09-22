package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.Projection;
import jakarta.persistence.criteria.JoinType;

public final class ProjectionFromClause implements FromClause {

    final SimpleFromClause simpleFromClause;

    public ProjectionFromClause(Projection projection) {
        this.simpleFromClause = new SimpleFromClause(new FragmentClause(projection.entity().getName()), projection.alias());
    }

    @Override
    public String getTableAlias() {
        return simpleFromClause.getTableAlias();
    }

    @Override
    public String toClauseString() {
        return simpleFromClause.toClauseString();
    }

    @Override
    public int getOrder() {
        return this.simpleFromClause.getOrder();
    }

    public SimpleJoinClause createJoinFromTo(JoinType joinType, FromClause joinClause,
                                             String mainField, String targetField) {
        return this.simpleFromClause.createJoinFromTo(joinType, joinClause, mainField, targetField);
    }

}
