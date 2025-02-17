package cn.procsl.ping.boot.jpa.support.query.ast;

import jakarta.persistence.criteria.JoinType;

import java.util.List;

public interface JoinExpression extends FromExpression {

    /**
     * join类型
     */
    JoinType getJoinType();

    /**
     * join 连接列名称
     */
    String getLeftColumnName();

    /**
     * join 连接列名称
     */

    String getRightColumnName();


    /**
     * 创建join语句
     * table_a as a inner join table_b as b on a.id = b.id
     * table_c as c inner join  table_a as a on c.xx=a.xx inner join table_b as b on a.id = b.id
     *
     * 1. 	inner join cn.procsl.ping.boot.jpa.support.query.SubEntity as sub1 on sub.id=sub1.id
     * 2. 	cn.procsl.ping.boot.jpa.support.query.MainEntity as main
     *
     * 1. 	cn.procsl.ping.boot.jpa.support.query.MainEntity as main
     *      inner join
     * 2. 	cn.procsl.ping.boot.jpa.support.query.SubEntity as sub
     *      on main.id = sub.id
     * 3.   inner join
     *  	cn.procsl.ping.boot.jpa.support.query.SubEntity as sub1
     *      on sub.id = sub1.id
     *
     */
    default String createJoinExpString(String leftTableAlias, String parentString) {


        DotExpression leftStr = new DotExpression(leftTableAlias, this.getLeftColumnName());
        DotExpression rightStr = new DotExpression(this.getTableAlias(), this.getRightColumnName());
        String equ = leftStr + "=" + rightStr;
        String t = this.getJoinType().toString().toLowerCase();
        String tmp = "\n\t%s join %s on %s".formatted(t, this.createFromExpString(), equ);

        List<JoinExpression> joins = this.getJoins();
        if (joins == null || joins.isEmpty()) {
            return tmp;
        }
        for (JoinExpression join : joins) {
            tmp += join.createJoinExpString(this.getTableAlias(), tmp);
        }
        return tmp;
    }
}
