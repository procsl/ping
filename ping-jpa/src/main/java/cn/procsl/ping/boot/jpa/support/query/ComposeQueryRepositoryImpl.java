package cn.procsl.ping.boot.jpa.support.query;

import cn.procsl.ping.boot.jpa.support.query.builder.QueryBuilderParseAdapter;
import cn.procsl.ping.boot.jpa.support.query.builder.QueryClauseParserImpl;
import cn.procsl.ping.boot.jpa.support.query.builder.def.PojoDefParser;
import jakarta.annotation.Nonnull;
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
    public <T> List<T> query(@Nonnull T query) {

        Class<?> clazz = query.getClass();
        PojoDefParser parser = new PojoDefParser(clazz);
        QueryClauseParserImpl impl = new QueryClauseParserImpl(parser);
        String queryString = this.adapter.buildQueryString(impl);
        log.info("查询语句: {}", queryString);
        return null;
    }

}
