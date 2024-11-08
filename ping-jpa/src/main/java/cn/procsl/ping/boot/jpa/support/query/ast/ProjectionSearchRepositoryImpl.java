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
        SimpleQueryStringBuilder builder = new SimpleQueryStringBuilder();
        Map<ClauseType, List<Clause>> results = list.stream().collect(Collectors.groupingBy(Clause::getClauseType));
        results.forEach((k, v) -> extracted(k, v, builder));
        String sql = builder.toClauseString();
        log.info("sql语句: {}", sql);
        return List.of();
    }

    private void extracted(ClauseType k, List<Clause> v, SimpleQueryStringBuilder builder) {
        if (k == ClauseType.select) {
            if (v instanceof SelectClause a) {
                builder.addSelectClause(a);
                return;
            }
            log.warn("类型错误: {}", v.getClass());
        }

        if (k == ClauseType.from) {
            if (v instanceof FromClause a) {
                builder.addFromClause(a);
                return;
            }
            log.warn("类型错误: {}", v.getClass());
        }

        if (k == ClauseType.order_by) {
            if (v instanceof OrderByClause a) {
                builder.addOrderBy(a);
                return;
            }
            log.warn("类型错误: {}", v.getClass());
        }

        if (k == ClauseType.where) {
            if (v instanceof WhereClause a) {
                builder.addWhereClause(a);
                return;
            }
            log.warn("类型错误: {}", v.getClass());
        }
    }

    /**
     * 初始化
     */
    @Override
    public void afterPropertiesSet() throws Exception {
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
