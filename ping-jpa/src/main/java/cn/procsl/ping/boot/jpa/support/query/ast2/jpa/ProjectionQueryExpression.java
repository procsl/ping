package cn.procsl.ping.boot.jpa.support.query.ast2.jpa;

import cn.procsl.ping.boot.jpa.support.query.ast2.FromExpression;
import cn.procsl.ping.boot.jpa.support.query.ast2.Expression;
import cn.procsl.ping.boot.jpa.support.query.ast2.SelectExpression;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RequiredArgsConstructor
final class ProjectionQueryExpression implements Expression {

    final private ArrayList<SelectExpression> selects = new ArrayList<>();
    final private ArrayList<FromExpression> froms = new ArrayList<>();

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
            .map(Expression::toExpString).collect(Collectors.joining(delimiter));

        String fromStr = froms.stream()
            .map(Expression::toExpString).collect(Collectors.joining(delimiter));

        String base = "select\n\t%s\nfrom\n\t%s";
        return base.formatted(selectStr, fromStr);
    }

    public void addSelect(SelectExpression select) {
        this.selects.add(select);
    }

    public void addFrom(FromExpression from) {
        this.froms.add(from);
    }

    /**
     * 获取sql占位符变量
     */
    public Set<Placeholder> getQueryVariables() {
        return null;
    }

    @Override
    public String toString() {
        return this.toExpString();
    }
}
