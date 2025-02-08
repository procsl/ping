package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.ProjectionSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Indexed
@Component("defaultProjectionSearchRepository")
class ProjectionSearchRepositoryImpl implements ProjectionSearchRepository, InitializingBean {

    List<QueryBuilder> builders = new ArrayList<>();

    @Override
    public <Q, R> List<R> search(Q query, Class<R> mapping) {
        QueryContext context = new InnerQueryContext(query, mapping);
        ArrayList<Clause> list = new ArrayList<>();
        for (QueryBuilder builder : builders) {
            Optional<List<? extends Clause>> result = builder.parse(context);
            result.ifPresent(list::addAll);
        }
        JpaQueryStringBuilder builder = new JpaQueryStringBuilder();
        Map<ClauseType, List<Clause>> results = list.stream().collect(Collectors.groupingBy(Clause::getClauseType));
        results.forEach((k, v) -> extracted(k, v, builder));
        String sql = builder.toClauseString();
        log.info("sql语句: \n\n{}\n", sql);
        return List.of();
    }

    private void extracted(ClauseType k, List<Clause> v, JpaQueryStringBuilder builder) {
        if (k == ClauseType.select) {
            for (Clause clause : v) {
                if (clause instanceof SelectClause a) {
                    builder.addSelectClause(a);
                }
            }
        }

        if (k == ClauseType.from) {
            for (Clause clause : v) {
                if (clause instanceof FromClause a) {
                    builder.addFromClause(a);
                }
            }
        }

        if (k == ClauseType.order_by) {
            for (Clause clause : v) {
                if (clause instanceof OrderByClause a) {
                    builder.addOrderBy(a);
                }
            }
        }

        if (k == ClauseType.where) {
            for (Clause clause : v) {
                if (clause instanceof WhereClause a) {
                    builder.addWhereClause(a);
                }
            }
        }
    }

    /**
     * 初始化
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.builders.add(new SelectClauseBuilder());
        this.builders.add(new FromClauseBuilder());
    }

    @RequiredArgsConstructor
    static class InnerQueryContext implements QueryContext {
        final Object query;
        final Class<?> mapping;

        @Override
        public Object getQueryInstance() {
            return query;
        }

        @Override
        public Class<?> getQueryClass() {
            return query.getClass();
        }

        @Override
        public Class<?> getMapping() {
            return mapping;
        }
    }

}
