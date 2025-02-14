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
//@Component("defaultProjectionSearchRepository")
class ProjectionSearchRepositoryImpl implements ProjectionSearchRepository, InitializingBean {

    List<QueryBuilder> builders = new ArrayList<>();

    @Override
    public <Q, R> List<R> search(Q query, Class<R> mapping) {
        QueryContext context = new InnerQueryContext(query, mapping);
        JpaQueryStringBuilder builder = new JpaQueryStringBuilder();
        for (QueryBuilder b : builders) {
            Optional<List<? extends Clause>> result = b.parse(context);
            result.ifPresent(item -> extracted(item, builder));
        }
        String sql = builder.toClauseString();
        log.info("sql语句: \n\n{}\n", sql);
        return List.of();
    }

    private void extracted(List<? extends Clause> v, JpaQueryStringBuilder builder) {
        for (Clause clause : v) {
            if (clause instanceof SelectClause a) {
                builder.addSelectClause(a);
            }
        }
        for (Clause clause : v) {
            if (clause instanceof FromClause a) {
                builder.addFromClause(a);
            }
        }
        for (Clause clause : v) {
            if (clause instanceof OrderByClause a) {
                builder.addOrderBy(a);
            }
        }
        for (Clause clause : v) {
            if (clause instanceof WhereClause a) {
                builder.addWhereClause(a);
            }
        }
    }

    /**
     * 初始化
     */
    @Override
    public void afterPropertiesSet() {
        this.builders.add(new SelectClauseBuilder());
        this.builders.add(new FromClauseBuilder());
        this.builders.add(new WhereClauseBuilder());
        this.builders.add(new OrderClauseBuilder());
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
