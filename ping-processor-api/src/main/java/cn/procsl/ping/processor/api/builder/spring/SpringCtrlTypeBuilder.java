package cn.procsl.ping.processor.api.builder.spring;

import cn.procsl.ping.processor.api.AnnotationVisitor;
import cn.procsl.ping.processor.api.AnnotationVisitorLoader;
import cn.procsl.ping.processor.api.Generator;
import cn.procsl.ping.processor.api.ProcessorContext;
import com.squareup.javapoet.TypeSpec;
import lombok.NonNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;

public class SpringCtrlTypeBuilder implements Generator<TypeSpec> {

    // 目标方法
    final TypeElement targetElement;

    final TypeSpec.Builder builder;

    final AnnotationVisitorLoader visitor;

    ServiceFieldBuilder fieldBuilder;

    private ProcessorContext context;

    public SpringCtrlTypeBuilder(@NonNull TypeElement targetElement, ProcessorContext context) {
        this.targetElement = targetElement;
        String name = this.getBaseTargetElementCtrlName();
        this.builder = TypeSpec.classBuilder(name);
        visitor = new AnnotationVisitorLoader(context, AnnotationVisitor.SupportType.CONTROLLER);
        fieldBuilder = new ServiceFieldBuilder(targetElement, context);
        this.context = context;
    }

    @Override
    public TypeSpec getSpec() {

        this.builder.addModifiers(Modifier.PUBLIC);
        visitor.typeVisitor(targetElement, this.builder);

        builder.addField(this.fieldBuilder.getSpec());

        targetElement.getEnclosedElements()
            .stream()
            .filter(item -> item instanceof ExecutableElement)
            .filter(this::methodSelector)
            .map(item -> (ExecutableElement) item)
            .map(item -> new ControllerMethodBuilder(item, this.context))
            .map(ControllerMethodBuilder::getSpec)
            .forEach(builder::addMethod);

        return builder.build();
    }

    boolean methodSelector(Element element) {
        Set<String> set = element.getAnnotationMirrors()
            .stream()
            .map(item -> item.getAnnotationType().asElement().toString())
            .collect(Collectors.toSet());
        return set.contains("javax.ws.rs.PATCH")
            || set.contains("javax.ws.rs.POST")
            || set.contains("javax.ws.rs.GET")
            || set.contains("javax.ws.rs.DELETE")
            || set.contains("javax.ws.rs.PATH")
            || set.contains("javax.ws.rs.PUT");
    }


    public String getBaseTargetElementCtrlName() {
        String name = targetElement.getSimpleName().toString();
        if (name.endsWith("Service")) {
            name = name.replaceAll("Application", "").replaceAll("Service" + "$", "Controller");
        } else {
            name = name + "Controller";
        }
        return name;
    }

}
