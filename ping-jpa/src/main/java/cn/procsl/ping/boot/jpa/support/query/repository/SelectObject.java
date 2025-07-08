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


    @Override
    default String toClauseString(Object context) {
        return selectClause() + " as " + aliasName();
    }
}
