package cn.procsl.ping.boot.jpa.support.query.ast;

public interface WhereExpression extends Expression {

    /**
     * 是否为必要条件
     */
    boolean isRequired();

    /**
     * 条件组名称
     */
    String groupName();

    /**
     * 是否包含查询
     */
    boolean isInclude();

    /**
     * 参数名称
     */
    Variable getParamVariable();

    /**
     * 条件表达式
     */
    Expression getExpression();

    /**
     * 条件
     */
    String condition();

    @Override
    default String toExpString() {
        return this.getExpression() + " " + this.condition() + " :" + this.getParamVariable();
    }
}
