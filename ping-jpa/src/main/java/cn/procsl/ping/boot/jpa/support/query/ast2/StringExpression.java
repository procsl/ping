package cn.procsl.ping.boot.jpa.support.query.ast2;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class StringExpression implements Expression {

    final String exp;

    @Override
    public String toExpString() {
        return exp;
    }

    @Override
    public String toString() {
        return this.toExpString();
    }
}
