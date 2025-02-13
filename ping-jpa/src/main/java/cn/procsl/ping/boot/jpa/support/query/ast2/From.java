package cn.procsl.ping.boot.jpa.support.query.ast2;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface From extends Node {

    /**
     * 获取表达式
     */
    Expression getExpression();

    /**
     * 获取别名
     */
    String getTableAlias();

    /**
     * 获取join节点
     */
    List<Join> getJoins();

    /**
     * 创建from语句, 不包含join
     */
    default String createFromExpString() {
        String main = this.getExpression().toExpString();
        return "%s as %s".formatted(main, this.getTableAlias());
    }


    /**
     * 创建表达式, 如果包含join列, 则会生成join列
     * table_a as a inner join table_b as b on a.id = b.id
     * table_c as c inner join  table_a as a on c.xx=a.xx inner join table_b as b on a.id = b.id
     */
    @Override
    default String toExpString() {

        List<Join> joins = this.getJoins();
        String main = this.createFromExpString();
        if (joins == null || joins.isEmpty()) {
            return main;
        }

        Function<Join, String> exp = item -> item.createJoinExpString(this);
        String tmp = joins.stream().map(exp).collect(Collectors.joining(" "));

        return main + " " + tmp;
    }

}
