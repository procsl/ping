package cn.procsl.ping.boot.jpa.support.query.ast;

import jakarta.persistence.criteria.JoinType;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class SimpleJoinClause implements FromClause {

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
        return " %s \n %s join %s on %s.%s = %s.%s".formatted(main,
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
