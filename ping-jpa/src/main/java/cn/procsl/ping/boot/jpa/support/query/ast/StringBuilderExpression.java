package cn.procsl.ping.boot.jpa.support.query.ast;

import java.util.ArrayList;
import java.util.Arrays;

public final class StringBuilderExpression implements Expression {

    private final ArrayList<String> items = new ArrayList<>();

    public StringBuilderExpression(String... items) {
        if (items != null && items.length > 0) {
            this.items.addAll(Arrays.asList(items));
        }
    }

    public StringBuilderExpression append(String item) {
        this.items.add(item);
        return this;
    }

    @Override
    public String toExpString() {
        return String.join("", items);
    }

    @Override
    public String toString() {
        return this.toExpString();
    }
}
