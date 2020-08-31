package cn.procsl.ping.boot.domain.support.executor;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

interface QueryHints extends Iterable<Map.Entry<String, Object>> {

    QueryHints withFetchGraphs(EntityManager em);

    QueryHints forCounts();

    Map<String, Object> asMap();

    enum NoHints implements QueryHints {

        INSTANCE;

        @Override
        public Map<String, Object> asMap() {
            return Collections.emptyMap();
        }

        @Override
        public Iterator<Map.Entry<String, Object>> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public QueryHints withFetchGraphs(EntityManager em) {
            return this;
        }

        @Override
        public QueryHints forCounts() {
            return this;
        }
    }
}
