package cn.procsl.ping.processor.restful.builder.model;

import cn.procsl.ping.processor.model.Annotation;
import cn.procsl.ping.processor.model.ClassTypeName;
import cn.procsl.ping.processor.model.Code;
import cn.procsl.ping.processor.model.TypeName;

import javax.annotation.processing.Generated;

public class GeneratedAnnotation implements Annotation {

    @Override
    public TypeName getType() {
        return new ClassTypeName(Generated.class);
    }

    @Override
    public Code getCode() {
        return Code.EMPTY_CODE;
    }


}
