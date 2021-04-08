package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.Model;
import lombok.NonNull;

public interface ModelConverter<S extends Model, T> {

    T to(@NonNull S source);

}
