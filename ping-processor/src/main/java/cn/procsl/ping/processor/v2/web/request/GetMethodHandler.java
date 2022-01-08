package cn.procsl.ping.processor.v2.web.request;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.v2.SpecHandler;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class GetMethodHandler implements SpecHandler<Supplier<MethodSpec.Builder>> {

    final String getMapping = "org.springframework.web.bind.annotation.GetMapping";


    @Override
    public void handle(Element element, Supplier<MethodSpec.Builder> builder, ProcessorEnvironment environment) {
        GET getter = element.getAnnotation(GET.class);
        if (getter == null) {
            return;
        }
        TypeElement mapping = environment.getTypeElementByName(getMapping);
        if (mapping == null) {
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR, "找不到注解依赖: " + getMapping, element);
            return;
        }
        MethodSpec.Builder method = builder.get();
        // 封装参数
        method.addParameters(((ExecutableElement) element).getParameters().stream().map(ParameterSpec::get).collect(Collectors.toList()));

        // 封装方法体 TODO

        // 添加注解
        AnnotationSpec.Builder annotation = AnnotationSpec.builder(ClassName.get(mapping));
        Path path = element.getAnnotation(Path.class);
        if (path == null) {
            path = element.getEnclosingElement().getAnnotation(Path.class);
        }
        if (path == null) {
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR, "找不到请求方法URL", element);
            return;
        }
        annotation.addMember("value", "%S", path.value());

        method.addAnnotation(annotation.build());
    }

}
