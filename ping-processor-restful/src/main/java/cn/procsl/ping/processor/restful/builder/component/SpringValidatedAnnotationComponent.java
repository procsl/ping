package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.component.AnnotationComponent;
import cn.procsl.ping.processor.component.CodeBlockComponent;
import cn.procsl.ping.processor.component.GeneralTypeNameComponent;
import cn.procsl.ping.processor.component.TypeNameComponent;
import cn.procsl.ping.processor.restful.utils.ClassUtils;

public class SpringValidatedAnnotationComponent implements AnnotationComponent {

    final String packageName = "org.springframework.validation.annotation";
    final String className = "Validated";
    final String template = "%s.%s";
    TypeNameComponent typeNameComponent;

    public SpringValidatedAnnotationComponent() {
        String fullName = String.format(template, packageName, className);
        if (ClassUtils.exists(fullName)) {
            typeNameComponent = new GeneralTypeNameComponent(packageName, className);
        } else {
            typeNameComponent = TypeNameComponent.NONE_TYPE;
        }
    }

    @Override
    public TypeNameComponent getType() {
        return typeNameComponent;
    }

    @Override
    public CodeBlockComponent getCode() {
        return CodeBlockComponent.EMPTY_CODE_BLOCK_COMPONENT;
    }


}
