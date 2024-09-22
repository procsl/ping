package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.ProjectionSearchRepository;
import cn.procsl.ping.boot.jpa.support.query.QueryBind;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.util.List;

@Slf4j
@Indexed
@Component("defaultProjectionSearchRepository")
class ProjectionSearchRepositoryImpl implements ProjectionSearchRepository {

    @Override
    public <Q, R> List<R> search(Q queryDetails, Class<R> resultMapping) {

        QueryBind bind = AnnotationUtils.findAnnotation(resultMapping, QueryBind.class);
        if (bind == null) {
            throw new IllegalArgumentException("ResultMapping is not annotated with @QueryBind");
        }
        String baseQL = bind.base();

        return List.of();
    }

}
