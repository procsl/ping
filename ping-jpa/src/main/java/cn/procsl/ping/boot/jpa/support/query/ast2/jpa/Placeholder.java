package cn.procsl.ping.boot.jpa.support.query.ast2.jpa;

import cn.procsl.ping.boot.jpa.support.query.ast2.Expression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 变量表达式
 */
@Getter
@RequiredArgsConstructor
public final class Placeholder implements Expression {

    final private String name;

    final private Class<?> type;

    @Override
    public String toExpString() {
        return ":" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Placeholder that = (Placeholder) o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }


    @Override
    public String toString() {
        return this.toExpString();
    }
}
