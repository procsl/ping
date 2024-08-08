package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.ComposeQueryRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Component
class ComposeQueryRepositoryImpl implements ComposeQueryRepository {
    @Override
    public <T> List<T> queryProjections(T query) {
        return List.of();
    }

    @Override
    public <T> List<T> query(T query) {
        return List.of();
    }

}
