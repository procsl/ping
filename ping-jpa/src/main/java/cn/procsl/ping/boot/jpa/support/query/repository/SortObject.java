package cn.procsl.ping.boot.jpa.support.query.repository;

public interface SortObject extends Clause {

    enum ORDER {
        asc, desc
    }

    String field();

    ORDER order();

    static SortObject forAsc(String field) {
        return new SimpleSortObject(field, ORDER.asc);
    }

    static SortObject forDesc(String field) {
        return new SimpleSortObject(field, ORDER.desc);
    }

    record SimpleSortObject(String field, ORDER order) implements SortObject {
        @Override
        public String toClauseString(BuilderContext context) {
            return field + " " + order;
        }
    }

}
