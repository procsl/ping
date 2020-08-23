package cn.procsl.ping.boot.domain.business.common.model;

import java.io.Serializable;

/**
 * 关系操作符
 */
public enum Operator implements Serializable {

    EQ("="), NE("!="), GE(">="), GT(">"), LE("<="), lt("<");

    String s;

    Operator(String s) {
        this.s = s;
    }

    public String getOperator() {
        return s;
    }
}
