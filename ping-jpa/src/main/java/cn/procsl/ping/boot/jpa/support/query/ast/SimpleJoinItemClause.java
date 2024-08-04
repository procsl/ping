package cn.procsl.ping.boot.jpa.support.query.ast;

import jakarta.persistence.criteria.JoinType;
import lombok.Getter;

public final class SimpleJoinItemClause implements JoinItemClause {

    @Getter
    private final String joinFieldName;

    @Getter
    private final FromClause fromClause;

    private final JoinType joinType;

    private final String targetAlias;

    private final String targetFieldName;

    public SimpleJoinItemClause(FromClause fromClause,
                                String currentJoinFieldName,
                                String targetAlias,
                                String targetFieldName,
                                JoinType joinType) {
        this.fromClause = fromClause;
        this.joinFieldName = currentJoinFieldName;
        this.targetAlias = targetAlias;
        this.targetFieldName = targetFieldName;
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
        return "%s.%s=%s.%s".formatted(this.targetAlias, this.targetFieldName, this.fromClause.getTableAlias(), this.joinFieldName);
    }

    @Override
    public String toString() {
        return this.toClauseString();
    }

    @Override
    public JoinType getJoinType() {
        return this.joinType;
    }


}
