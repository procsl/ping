package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class SimpleParameter implements Parameter {

    final String name;
    final Class<?> type;
    final Object value;
}
