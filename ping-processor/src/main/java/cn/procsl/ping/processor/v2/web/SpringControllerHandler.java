package cn.procsl.ping.processor.v2.web;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.utils.NamingUtils;
import cn.procsl.ping.processor.v2.SpecHandler;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpringControllerHandler implements SpecHandler<TypeSpec.Builder> {

    final String httpServletRequest = "javax.servlet.http.HttpServletRequest";
    final List<SpecHandler<MethodSpec.Builder>> methodHandlers;

    public SpringControllerHandler() {
        methodHandlers = Arrays.asList(new GetMethodHandler(), new PostRequestMethodHandler());
    }

    @Override
    public void handle(Element source, TypeSpec.Builder builder, ProcessorEnvironment environment) {
        if (!(source instanceof TypeElement)) {
            return;
        }

        TypeElement fieldRef = environment.getTypeElementByName(httpServletRequest);
        if (fieldRef != null) {
            TypeName type = TypeName.get(fieldRef.asType());
            String fieldName = NamingUtils.lowerCamelCase(fieldRef.getSimpleName().toString());
            FieldSpec.Builder request = FieldSpec.builder(type, fieldName, Modifier.PROTECTED);
            builder.addField(request.build());
        }

        for (Element item : source.getEnclosedElements()) {
            MethodSpec method = this.buildMethod(item, environment);
            builder.addMethod(method);
        }
    }

    MethodSpec buildMethod(Element item, ProcessorEnvironment env) {
        String methodName = item.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC);
        // 通过不同的类创建不同的方法
        for (SpecHandler<MethodSpec.Builder> handler : this.methodHandlers) {
            handler.handle(item, methodBuilder, env);
        }
        return methodBuilder.build();
    }

//    boolean methodSelector(Element element) {
//        Set<String> set = element.getAnnotationMirrors().stream().map(item -> item.getAnnotationType().asElement().toString()).collect(Collectors.toSet());
//        return set.contains("javax.ws.rs.PATCH") || set.contains("javax.ws.rs.POST") || set.contains("javax.ws.rs.GET") || set.contains("javax.ws.rs.DELETE") || set.contains("javax.ws.rs.PATH") || set.contains("javax.ws.rs.PUT");
//    }

    @Override
    public int order() {
        return Integer.MAX_VALUE - 1;
    }
}
