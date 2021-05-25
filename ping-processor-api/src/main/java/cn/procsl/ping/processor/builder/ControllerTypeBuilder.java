package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.model.*;
import cn.procsl.ping.processor.utils.NamingUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
public class ControllerTypeBuilder extends TypeModel {

    @NonNull TypeElement typeElement;

    @NonNull String prefix;

    @Override
    public NamingModel getType() {
        return new NamingModel(typeElement.getEnclosingElement().toString(), typeElement.getSimpleName().toString() + "Controller");
    }

    @Override
    public Collection<Modifier> getModifiers() {
        return Collections.singleton(Modifier.PUBLIC);
    }

    @Override

    public Collection<AnnotationModel> getAnnotations() {
        return Arrays.asList(
            new GeneratedAnnotationBuilder(),
            new AnnotationModel(new NamingModel("org.springframework.web.bind.annotation", "RestController")),
            new RequestMappingAnnotationBuilder(prefix, typeElement)
        );
    }


    @Override
    public Collection<FieldModel> getFields() {
        FieldModel field = new FieldModel();
        field.setType(new NamingModel(typeElement.getEnclosingElement().toString(), typeElement.getSimpleName().toString()));
        field.setModifiers(Collections.singleton(Modifier.PUBLIC));
        field.setParent(this);
        field.setName(NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString()));
        field.setAnnotations(Collections.singletonList(
            new AnnotationModel(new NamingModel("org.springframework.beans.factory.annotation", "Autowired"))
        ));
        return Collections.singleton(field);
    }

    @Override
    public Collection<MethodModel> getMethods() {
        return typeElement.getEnclosedElements()
            .stream()
            .filter(item -> item instanceof ExecutableElement)
            .map(item -> (ExecutableElement) item)
            .filter(item -> item.getModifiers().contains(Modifier.PUBLIC))
            .map(item -> {
                MethodModel method = new MethodModel();
                method.setModifiers(Collections.singleton(Modifier.PUBLIC));
                method.setName(item.getSimpleName().toString());
                method.setParent(ControllerTypeBuilder.this);
//                method.setReturned(new NamingModel(item.getReturnType()));
                return method;
            })
            .collect(Collectors.toList());
    }
}
