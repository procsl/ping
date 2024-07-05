package cn.procsl.ping.boot.jpa.support.query.builder;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
final public class SimpleQueryStringBuilder implements QueryStringBuilder {

    int len = 50;
    private final List<String> selects = new ArrayList<>();
    private final List<String> from = new ArrayList<>();
    private final List<String> wheres = new ArrayList<>();
    private final List<String> orderBy = new ArrayList<>();
    private final List<String> groupBy = new ArrayList<>();

    public <T extends Clause> void addSelect(T select) {
        len = this.addTo("select", select, selects);
    }

    public <T extends Clause> void addWhere(T where) {
        len += this.addTo("where", where, this.wheres);
    }

    public <T extends Clause> void addOrderBy(T orderBy) {
        len += this.addTo("order by", orderBy, this.orderBy);
    }

    public <T extends Clause> void addGroupBy(T groupBy) {
        len += this.addTo("group by", groupBy, this.groupBy);
    }

    public <T extends Clause> void addFrom(T from) {
        len += this.addTo("from", from, this.from);
    }

    @Override
    public String buildQueryString() {

        if (this.selects.isEmpty()) {
            throw new IllegalArgumentException("No select specified");
        }

        if (this.from.isEmpty()) {
            throw new IllegalArgumentException("No from specified");
        }

        String selectString = String.join(",", this.selects);
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

    int addTo(String name, @NonNull Clause string, List<String> contains) {
        String clause = string.toClauseString().trim();
        if (clause.isEmpty()) {
            return 0;
        }

        if (clause.contains(",")) {
            throw new IllegalArgumentException(name + "参数不应存在`,`");
        }

        if (contains.contains(clause)) {
            throw new IllegalArgumentException(name + " exists: " + string);
        }
        contains.add(clause);
        return clause.length() + 1;
    }

}
