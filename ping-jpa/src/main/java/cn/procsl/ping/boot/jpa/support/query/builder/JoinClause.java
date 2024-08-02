package cn.procsl.ping.boot.jpa.support.query.builder;

import jakarta.persistence.criteria.JoinType;

public interface JoinClause extends FromClause {

    JoinType getJoinType();

    String getJoinMainEntity();

}
