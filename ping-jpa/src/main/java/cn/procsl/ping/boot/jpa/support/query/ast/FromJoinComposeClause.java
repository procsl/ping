package cn.procsl.ping.boot.jpa.support.query.ast;

import jakarta.persistence.criteria.JoinType;

import java.util.ArrayList;
import java.util.List;

public final class FromJoinComposeClause implements FromClause {

    private FromClause fromClause;
    private List<JoinItemClause> joins = new ArrayList<>();

    @Override
    public String getTableAlias() {
        return fromClause.getTableAlias();
    }

    @Override
    public JoinItemClause toJoinOn(JoinType joinType, FromClause joinClause, String mainField, String targetField) {
        return fromClause.toJoinOn(joinType, joinClause, mainField, targetField);
    }

    public void addJoinOn(JoinItemClause joinClause) {
        this.joins.add(joinClause);
    }

    @Override
    public String toClauseString() {
        //获取当前 main 的 别名


        return "";
    }

}
