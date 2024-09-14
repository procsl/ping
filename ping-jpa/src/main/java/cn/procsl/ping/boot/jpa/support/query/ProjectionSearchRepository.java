package cn.procsl.ping.boot.jpa.support.query;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ProjectionSearchRepository {

    <Q, R> List<R> search(Q queryDetails, Class<R> resultMapping);

}
