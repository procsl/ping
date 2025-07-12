package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.NonNull;

public interface SelectObject extends Clause {

    /**
     * @return 返回查询语句
     */
    @NonNull
    String selectClause();

    /**
     * @return 查询语句对应别名
     */
    @NonNull
    String aliasName();


    default String toClauseString(String context) {
        return String.format("%s as %s", selectClause(), aliasName());
    }
}
