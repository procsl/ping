package cn.procsl.ping.boot.jpa.support.query.ast;

import java.util.List;

public interface ResultExtractor<T> {

    T extract(List<Result> results);

}
