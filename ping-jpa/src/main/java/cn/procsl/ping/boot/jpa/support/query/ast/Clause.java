package cn.procsl.ping.boot.jpa.support.query.ast;

public interface Clause {

    /**
     * 生成的查询语句片段
     */
    String toClauseString();


}
