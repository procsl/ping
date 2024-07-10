package cn.procsl.ping.boot.jpa.support.query;

import cn.procsl.ping.boot.jpa.support.query.builder.QueryBuilderParseAdapter;
import cn.procsl.ping.boot.jpa.support.query.builder.parser.AnnotationClauseParser;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ComposeQueryRepositoryImpl implements ComposeQueryRepository {

    final EntityManager em;

    final QueryBuilderParseAdapter adapter = new QueryBuilderParseAdapter();

    @Override
    public <T> List<T> queryProjections(T query) {


        Projection projection = AnnotationUtils.findAnnotation(query.getClass(), Projection.class);
        if (projection == null) {
            throw new IllegalArgumentException("Query class must have @QueryEntity annotation");
        }


        return List.of();
    }

    @Override
    @SneakyThrows
    public <T> List<T> query(T query) {
        final AnnotationClauseParser parser = new AnnotationClauseParser(query.getClass());

        String jpql = this.adapter.buildQueryString(parser);

        return null;
    }

}
