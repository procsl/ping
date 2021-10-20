package cn.procsl.ping.processor.model;

import cn.procsl.ping.processor.Model;

public interface Annotation extends Model {

    TypeName getType();

    @Override
    default String generatorType() {
        return "annotation";
    }

    Code getCode();

}
