package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.ProjectionSearchRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.util.List;

@Slf4j
@Indexed
@Component("defaultProjectionSearchRepository")
class ProjectionSearchRepositoryImpl implements ProjectionSearchRepository {


    @Override
    public <T> List<T> search(@NonNull T query) {
        ProjectionFieldDescription projection = new ProjectionFieldDescription(query.getClass());
        QueryAnnotationParser parser = new QueryAnnotationParser(projection);
        String sql = parser.toClauseString();
        log.debug("生成的SQL语句为: {}", sql);
        return List.of();
    }

}
