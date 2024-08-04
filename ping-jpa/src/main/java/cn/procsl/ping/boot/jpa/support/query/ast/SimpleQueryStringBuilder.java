package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
final public class SimpleQueryStringBuilder implements QueryClause {

    private int len = 50;
    private final Map<String, SelectClause> selects = new HashMap<>();
    private final List<String> from = new ArrayList<>();
    private final List<String> wheres = new ArrayList<>();
    private final List<String> orderBy = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();


    @Override
    public String toClauseString() {
        if (this.selects.isEmpty()) {
            throw new IllegalArgumentException("No select specified");
        }

        if (this.from.isEmpty()) {
            throw new IllegalArgumentException("No from specified");
        }

        String selectString = this.selects.values().stream()
            .sorted(Comparator.comparingInt(SelectClause::getOrder))
            .map(Clause::toClauseString).collect(Collectors.joining(","));
        String fromString = String.join(",", this.from);

        StringBuilder sb = new StringBuilder(len);
        sb.append("select ");
        sb.append(selectString);
        sb.append(" from ");
        sb.append(fromString);
        if (!this.wheres.isEmpty()) {
            sb.append(" where ");
            sb.append(String.join(",", this.wheres));
        }

        if (!this.groupBy.isEmpty()) {
            sb.append(" group by ");
            sb.append(String.join(",", this.groupBy));
        }

        if (!this.orderBy.isEmpty()) {
            sb.append(" order by ");
            sb.append(String.join(",", this.orderBy));
        }
        if (len < sb.length()) {
            log.warn("Query string 长度预估错误");
        }
        return sb.toString().trim();
    }

    @Override
    public void addSelectClause(SelectClause selectClause) {
        if (this.selects.containsKey(selectClause.getSelectFieldAlias())) {
            throw new IllegalArgumentException(selectClause.getSelectFieldAlias() + " exists: " + selectClause.toClauseString());
        }

        this.selects.put(selectClause.getSelectFieldAlias(), selectClause);
    }

    @Override
    public void addFromClause(FromClause fromClause) {

    }

    @Override
    public String toString() {
        return this.toClauseString();
    }

}
