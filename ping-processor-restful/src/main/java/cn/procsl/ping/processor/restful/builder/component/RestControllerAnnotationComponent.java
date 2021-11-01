package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.component.AnnotationComponent;
import cn.procsl.ping.processor.component.ClassTypeNameComponent;
import cn.procsl.ping.processor.component.CodeBlockComponent;
import cn.procsl.ping.processor.component.TypeNameComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.TypeElement;

@RequiredArgsConstructor
public class RestControllerAnnotationComponent implements AnnotationComponent {

    final TypeElement typeElement;

    @Override
    public TypeNameComponent getType() {
        return new ClassTypeNameComponent(RestController.class);
    }

    @Override
    public CodeBlockComponent getCode() {
        // TODO
//        Code code = new Code();
//        code.addCodeParameters(NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString()));
        return null;
    }
}
