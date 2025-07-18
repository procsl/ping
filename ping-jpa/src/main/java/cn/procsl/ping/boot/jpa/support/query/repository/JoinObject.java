package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.NonNull;

public interface JoinObject extends Clause {

    enum JoinType implements Clause {
        inner, left;

        @Override
        public String toClauseString(BuilderContext context) {
            return (this + " join");
        }


        @Override
        public String toString() {
            if (this == inner) {
                return "inner";
            }
            if (this == left) {
                return "left ";
            }
            return super.toString();
        }
    }

    // join类型
    @NonNull
    JoinObject.JoinType getJoinType();

    // 被join的from对象
    @NonNull
    FromObject targetFromObject();

    Operator getOperator();

    // Table_A as a inner join Table_B as b on a.id = b.id
    // inner join Table_C as c on b.id = c.id
    // 如果 Table_C 为 无join关系表
    // 1. 返回  inner join Table_C as c on condition_a
    // 如果 Table_B 为有join关系的表
    // 2. 返回  inner join Table_B as b on condition_b inner join Table_C as c on condition_a
    @Override
    default String toClauseString(BuilderContext context) {
        // 1. Table_D as d
        // 2. Table_C as c inner join [1 table_x as x] on [operator_c]
        // 3. Table_B as b inner join [2 table_x as x] on [operator_b]

        // 获取被join的from的语句
        String formStr = this.targetFromObject().toBaseClauseString(context);
        String joins = this.targetFromObject().toJoinClauseString(context);
        if (joins == null || joins.isEmpty()) {
            joins = "";
        }
        String tt = this.getJoinType().toClauseString(context);
        return "%s %s on %s%s".formatted(tt, formStr, this.getOperator().toClauseString(context), joins);
    }


}
