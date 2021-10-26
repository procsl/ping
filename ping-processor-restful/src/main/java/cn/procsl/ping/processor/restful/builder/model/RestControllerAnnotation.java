package cn.procsl.ping.processor.restful.builder.model;

import cn.procsl.ping.processor.model.Annotation;
import cn.procsl.ping.processor.model.ClassTypeName;
import cn.procsl.ping.processor.model.Code;
import cn.procsl.ping.processor.model.TypeName;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.TypeElement;

@RequiredArgsConstructor
public class RestControllerAnnotation implements Annotation {

    final TypeElement typeElement;

    @Override
    public TypeName getType() {
        return new ClassTypeName(RestController.class);
    }

    @Override
    public Code getCode() {
        // TODO
//        Code code = new Code();
//        code.addCodeParameters(NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString()));
        return null;
    }
}
