package cn.procsl.ping.boot.domain.support;

import com.querydsl.core.types.dsl.BeanPath;

public interface PathResolver {

    <T> BeanPath<T> createPath(Class<T> clazz);

}
