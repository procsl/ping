package cn.procsl.ping.boot.jpa.support.query.ast;

import jakarta.persistence.criteria.JoinType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
final class SimpleFromClause implements FromClause {

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

    public SimpleJoinClause createJoinFromTo(JoinType joinType, FromClause joinClause,
                                             String mainField, String targetField) {
        return new SimpleJoinClause(this, mainField, joinClause.getTableAlias(), targetField, joinType);
    }

}
