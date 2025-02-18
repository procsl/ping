package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.ast.*;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
final class ProjectionQueryExpression implements Expression {

    final private ArrayList<SelectExpression> selects = new ArrayList<>();
    final private ArrayList<FromExpression> froms = new ArrayList<>();
    final private ArrayList<WhereExpression> wheres = new ArrayList<>();
    final private ArrayList<OrderFieldExpression> orders = new ArrayList<>();

    final private String delimiter;

    public ProjectionQueryExpression(boolean formatter) {
//        if (formatter) {
//            delimiter = ",\n";
////        } else {
//            delimiter = ",";
//        }
        delimiter = ",\n\t";
    }

    @Override
    public String toExpString() {

        String selectStr = selects.stream()
            .filter(SelectExpression::isInclude)
            .map(Expression::toExpString).collect(Collectors.joining(delimiter));

        String fromStr = froms.stream()
            .map(Expression::toExpString).collect(Collectors.joining(delimiter));

        AtomicInteger i = new AtomicInteger(0);
        Map<String, List<WhereExpression>> group = wheres.stream()
            .filter(WhereExpression::isInclude)
            .collect(Collectors.groupingBy(item -> {
                if (item.groupName() == null || item.groupName().isEmpty()) {
                    return i.incrementAndGet() + "";
                }
                return item.groupName();
            }));

        ArrayList<String> list = new ArrayList<>();
        group.forEach((k, v) -> {
            if (v.size() == 1) {
                list.add(v.getFirst().toExpString());
            } else {
                String collect = v.stream().map(WhereExpression::toExpString).collect(Collectors.joining(" or "));
                list.add("(" + collect + ")");
            }
        });


        String whereStr = list.isEmpty() ? "" : "\nwhere\n\t" + String.join(" and ", list);
        String ordersStr =
            this.orders.isEmpty() ? "" : "\norder by\n\t" + orders.stream()
                .sorted(Comparator.comparingInt(OrderFieldExpression::sort))
                .map(Expression::toExpString).collect(Collectors.joining(","));

        String base = "select\n\t%s\nfrom\n\t%s";
        return base.formatted(selectStr, fromStr) + whereStr + ordersStr;
    }

    public void addSelect(SelectExpression select) {
        this.selects.add(select);
    }

    public void addFrom(FromExpression from) {
        this.froms.add(from);
    }

    public void addWhere(WhereExpression where) {
        this.wheres.add(where);
    }

    public void addOrder(OrderFieldExpression order) {
        this.orders.add(order);
    }

    /**
     * 获取sql占位符变量
     */
    public Set<Variable> getQueryVariables() {
        return this.wheres.stream()
            .filter(WhereExpression::isInclude)
            .map(WhereExpression::getParamVariable)
            .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return this.toExpString();
    }
}
