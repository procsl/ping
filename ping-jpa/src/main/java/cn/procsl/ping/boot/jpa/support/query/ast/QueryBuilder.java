package cn.procsl.ping.boot.jpa.support.query.ast;

import java.util.List;
import java.util.Optional;

public interface QueryBuilder {


    /**
     * 解析的目标语句
     *
     * @param clazz 目标实体
     * @return 解析后的语句
     */
    Optional<List<? extends Clause>> parse(QueryContext context);

}
