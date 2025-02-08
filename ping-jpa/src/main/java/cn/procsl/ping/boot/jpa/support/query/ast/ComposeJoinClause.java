package cn.procsl.ping.boot.jpa.support.query.ast;

import jakarta.persistence.criteria.JoinType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
final class ComposeJoinClause implements FromClause {

    final private FromClause baseFrom;

    final private List<SimpleJoinClause> joinClauses = new ArrayList<>();

    @Getter
    private int order;


    @Override
    public String toString() {
        return this.toClauseString();
    }

    @Override
    public String getTableAlias() {
        return baseFrom.getTableAlias();
    }

    @Override
    public String toClauseString() {
        String base = baseFrom.toClauseString();
        if (joinClauses.isEmpty()) {
            return base;
        }

        joinClauses.sort(Comparator.comparingInt(FromClause::getOrder));
        return null;
    }

    // table_a as a inner join table_b as b on a.a_id = b.b_id
//    public void joinTo(JoinType joinType, FromClause joinClause, String mainTableAlias,
//                       String mainField, String targetField) {
//        SimpleJoinClause tmp = new SimpleJoinClause(joinClause, mainField, mainTableAlias, targetField, joinType);
//        this.joinClauses.add(tmp);
//    }
}
