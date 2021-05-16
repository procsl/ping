package cn.procsl.ping.processor.converter;

import lombok.NonNull;

public interface ModelConverter<S, T> {

    T convertTo(@NonNull S source);

}
