package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.builder.Alias;
import cn.procsl.ping.boot.jpa.support.query.builder.Clause;

/**
 * 字段描述对象
 */
public interface QuerySelect extends Clause, Alias {

    /**
     * 字段名称
     */
    String fieldName();

}
