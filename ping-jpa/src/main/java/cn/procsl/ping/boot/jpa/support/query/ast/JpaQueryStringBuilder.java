package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
final class JpaQueryStringBuilder implements QueryClause {

    private int len = 50;
    private final Map<String, SelectClause> selects = new HashMap<>();
    private final List<FromClause> from = new ArrayList<>();
    private final List<WhereClause> wheres = new ArrayList<>();
    private final List<OrderByClause> orderBy = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();


    @Override
    public String toClauseString() {
        if (this.selects.isEmpty()) {
            throw new IllegalArgumentException("No select specified");
        }

        if (this.from.isEmpty()) {
            throw new IllegalArgumentException("No from specified");
        }

        Comparator<Order> comparing = Comparator.comparingInt(Order::getOrder);

        String selectString = this.selects.values().stream()
            .sorted(comparing)
            .map(Clause::toClauseString).collect(Collectors.joining(",\n\t"));

        String fromString = this.from.stream()
            .sorted(comparing)
            .map(Clause::toClauseString).collect(Collectors.joining(",\n\t"));

        StringBuilder sb = new StringBuilder(len);
        sb.append("select \n\t");
        sb.append(selectString);
        sb.append("\nfrom \n\t");
        sb.append(fromString);
        sb.append("\n");

        if (!this.wheres.isEmpty()) {
            sb.append(" where ");
            this.wheres.sort(comparing);
            String whereString = this.wheres.stream()
                .sorted(comparing)
                .map(Clause::toClauseString).collect(Collectors.joining(" "));
            sb.append(whereString);
        }

        if (!this.groupBy.isEmpty()) {
            sb.append(" group by ");
            sb.append(String.join(",", this.groupBy));
        }

        if (!this.orderBy.isEmpty()) {
            sb.append(" order by ");
            this.orderBy.sort(comparing);
            String orderString = this.wheres.stream()
                .sorted(comparing)
                .map(Clause::toClauseString).collect(Collectors.joining(" "));
            sb.append(orderString);
        }
        if (len < sb.length()) {
//            log.warn("Query string 长度预估错误");
        }
        return sb.toString();
    }

    @Override
    public void addSelectClause(SelectClause selectClause) {
        if (this.selects.containsKey(selectClause.getSelectFieldAlias())) {
            throw new IllegalArgumentException(selectClause.getSelectFieldAlias() + " exists: "
                + selectClause.toClauseString());
        }

        this.selects.put(selectClause.getSelectFieldAlias(), selectClause);
    }

    @Override
    public void addFromClause(FromClause fromClause) {
        this.from.add(fromClause);
    }

    @Override
    public void addWhereClause(WhereClause whereClause) {
        this.wheres.add(whereClause);
    }

    @Override
    public void addOrderBy(OrderByClause orderByClause) {
        this.orderBy.add(orderByClause);
    }

    @Override
    public String toString() {
        return this.toClauseString();
    }

}
