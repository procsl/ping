package cn.procsl.ping.boot.jpa.support.query;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ProjectionSearchRepository {

    <T> List<T> search(T query);

}
