package cn.procsl.ping.boot.jpa.support.query.ast;

import jakarta.persistence.criteria.JoinType;

public interface JoinItemClause extends Clause {

    /**
     * 获取from类型
     */
    FromClause getFromClause();

    /**
     * 获取join类型
     *
     * @return
     */
    JoinType getJoinType();
}
