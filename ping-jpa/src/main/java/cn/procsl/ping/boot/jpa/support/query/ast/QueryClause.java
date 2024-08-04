package cn.procsl.ping.boot.jpa.support.query.ast;


public interface QueryClause extends Clause {

    void addSelectClause(SelectClause selectClause);

    void addFromClause(FromClause fromClause);

//    void addWhere();

//    void addOrderBy();

//    void addGroupBy();

}
