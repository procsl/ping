package cn.procsl.ping.boot.jpa.support.query.ast2.jpa;

import cn.procsl.ping.boot.jpa.support.query.ast2.From;
import cn.procsl.ping.boot.jpa.support.query.ast2.Node;
import cn.procsl.ping.boot.jpa.support.query.ast2.Select;

import java.util.ArrayList;
import java.util.stream.Collectors;

final class JpaQueryExpression implements Node {

    final ArrayList<Select> selects = new ArrayList<>();
    final ArrayList<From> froms = new ArrayList<>();

    @Override
    public String toExpString() {

        String selectStr = selects.stream()
            .map(Node::toExpString).collect(Collectors.joining(","));

        String fromStr = froms.stream()
            .map(Node::toExpString).collect(Collectors.joining(","));

        String base = "select %s from %s";
        return base.formatted(base, selectStr, fromStr);
    }

    public void addSelect(Select select) {
        this.selects.add(select);
    }

    public void addFrom(From from) {
        this.froms.add(from);
    }


}
