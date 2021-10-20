package cn.procsl.ping.processor.restful.builder.model;

import cn.procsl.ping.processor.model.Annotation;
import cn.procsl.ping.processor.model.Code;
import cn.procsl.ping.processor.model.GeneralTypeName;
import cn.procsl.ping.processor.model.TypeName;
import cn.procsl.ping.processor.restful.utils.ClassUtils;

public class SpringValidatedAnnotation implements Annotation {

    final String packageName = "org.springframework.validation.annotation";
    final String className = "Validated";
    final String template = "%s.%s";
    TypeName typeName;

    public SpringValidatedAnnotation() {
        String fullName = String.format(template, packageName, className);
        if (ClassUtils.exists(fullName)) {
            typeName = new GeneralTypeName(packageName, className);
        } else {
            typeName = TypeName.NONE_TYPE;
        }
    }

    @Override
    public TypeName getType() {
        return typeName;
    }

    @Override
    public Code getCode() {
        return Code.EMPTY_CODE;
    }


}
