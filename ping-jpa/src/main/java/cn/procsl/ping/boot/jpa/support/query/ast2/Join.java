package cn.procsl.ping.boot.jpa.support.query.ast2;

import java.util.List;
import java.util.stream.Collectors;

public interface Join extends From {

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

    enum JoinType {
        inner, left, right
    }

    /**
     * 创建join语句
     * table_a as a inner join table_b as b on a.id = b.id
     * table_c as c inner join  table_a as a on c.xx=a.xx inner join table_b as b on a.id = b.id
     */
    default String createJoinExpString(From left) {

        List<Join> joins = this.getJoins();
        if (joins == null || joins.isEmpty()) {
            String leftStr = left.getTableAlias() + "." + this.getLeftColumnName();
            String rightStr = this.getTableAlias() + "." + this.getRightColumnName();
            String equ = leftStr + "=" + rightStr;
            return "%s join %s on %s".formatted(this.getJoinType(), this.createFromExpString(), equ);
        }

        return joins.stream().map(item -> item.createJoinExpString(this)).collect(Collectors.joining(" "));
    }
}
