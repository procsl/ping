package cn.procsl.ping.boot.jpa.support.query.ast;


public interface QueryClause extends Clause {

    void addSelectClause(SelectClause selectClause);

    void addFromClause(FromClause fromClause);

    void addWhereClause(WhereClause whereClause);

    void addOrderBy(OrderByClause orderByClause);

//    void addGroupBy();

}
