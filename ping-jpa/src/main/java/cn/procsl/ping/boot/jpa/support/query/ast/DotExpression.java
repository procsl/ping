package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DotExpression implements Expression {

    final String main;
    final String name;

    @Override
    public String toExpString() {
        return main + "." + name;
    }

    @Override
    public String toString() {
        return this.toExpString();
    }


}
