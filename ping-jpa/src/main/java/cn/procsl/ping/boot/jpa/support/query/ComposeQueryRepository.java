package cn.procsl.ping.boot.jpa.support.query;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ComposeQueryRepository {

    <T> List<T> queryProjections(T query);

    <T> List<T> query(T query);

}
