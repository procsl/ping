package cn.procsl.ping.boot.jpa.support.query.ast2;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class AliasExpression implements Expression {

    final private Expression expression;
    final private String alias;

    @Override
    public String toExpString() {
        return expression.toExpString() + " as " + alias;
    }


    @Override
    public String toString() {
        return this.toExpString();
    }

}
