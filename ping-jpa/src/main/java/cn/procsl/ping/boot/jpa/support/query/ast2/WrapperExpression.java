package cn.procsl.ping.boot.jpa.support.query.ast2;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WrapperExpression implements Expression {

    private final Expression expression;

    @Override
    public String toExpString() {
        return "(" + expression.toExpString() + ")";
    }


    @Override
    public String toString() {
        return this.toExpString();
    }

}
