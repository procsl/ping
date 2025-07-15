package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.NonNull;
import lombok.Setter;

import java.util.*;

@Setter
public final class SimpleQueryObjectBuilder implements SearchObject {

    private final List<SelectObject> select = new ArrayList<>();
    private final List<FromObject> from = new ArrayList<>();
    private final List<SortObject> sort = new ArrayList<>();
    private WhereObject where;
    private final static NoneWhere none = new NoneWhere();

    @Override
    public List<SelectObject> select() {
        return select;
    }

    @Override
    public List<FromObject> from() {
        return from;
    }

    @Override
    public List<SortObject> sort() {
        return sort;
    }

    @Override
    public WhereObject where() {
        return where == null ? none : where;
    }

    public void addSelect(SelectObject... selectObject) {
        if (selectObject == null) {
            return;
        }
        this.select.addAll(Arrays.asList(selectObject));
    }

    public void addSelect(String selectClause, String aliasName) {
        this.select.add(new TSelect(selectClause, aliasName));
    }

    public void addFrom(String baseFormClause, String aliasName) {
        this.from.add(new SimpleFrom(baseFormClause, aliasName));
    }

    public void addFrom(FromObject... fromObject) {
        if (fromObject == null) {
            return;
        }
        this.from.addAll(Arrays.asList(fromObject));
    }

    public void addSort(SortObject... sortObject) {
        if (sortObject == null) {
            return;
        }
        this.sort.addAll(Arrays.asList(sortObject));
    }

    record TSelect(String selectClause, String aliasName) implements SelectObject {
        @Override
        public @NonNull String selectClause() {
            return selectClause;
        }

        @Override
        public @NonNull String aliasName() {
            return aliasName;
        }
    }

    record NoneWhere() implements WhereObject {

        @Override
        public Operator getOperator() {
            return Operator.NONE;
        }

        @Override
        public Set<Argument> getArguments() {
            return Collections.emptySet();
        }

        @Override
        public String toClauseString(BuilderContext context) {
            return "";
        }
    }

}
