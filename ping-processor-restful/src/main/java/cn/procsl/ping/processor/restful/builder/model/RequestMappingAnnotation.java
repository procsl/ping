package cn.procsl.ping.processor.restful.builder.model;

import cn.procsl.ping.processor.model.Annotation;
import cn.procsl.ping.processor.model.ClassTypeName;
import cn.procsl.ping.processor.model.Code;
import cn.procsl.ping.processor.model.TypeName;
import org.springframework.web.bind.annotation.RequestMapping;

public class RequestMappingAnnotation implements Annotation {


    @Override
    public TypeName getType() {
        return new ClassTypeName(RequestMapping.class);
    }

    @Override
    public Code getCode() {
        return Code.EMPTY_CODE;
    }
}
