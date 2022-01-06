package cn.procsl.ping.processor.v2.web;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.utils.NamingUtils;
import cn.procsl.ping.processor.v2.TypeSpecHandler;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpringControllerHandler implements TypeSpecHandler {

    final String httpServletRequest = "javax.servlet.http.HttpServletRequest";

    @Override
    public void handle(Element source, TypeSpec.Builder builder, ProcessorEnvironment environment) {
        if (!(source instanceof TypeElement)) {
            return;
        }

        String fieldName = null;

        TypeElement fieldRef = environment.getTypeElementByName(httpServletRequest);
        if (fieldRef != null) {
            TypeName type = TypeName.get(fieldRef.asType());
            fieldName = NamingUtils.lowerCamelCase(fieldRef.getSimpleName().toString());
            FieldSpec.Builder request = FieldSpec.builder(type, fieldName, Modifier.PROTECTED);
            builder.addField(request.build());
        }

        List<ExecutableElement> list = source.getEnclosedElements().stream().filter(item -> item instanceof ExecutableElement).filter(this::methodSelector).map(item -> (ExecutableElement) item).collect(Collectors.toList());
        for (ExecutableElement item : list) {
            MethodSpec method = this.buildMethod((TypeElement) source, item, fieldRef, fieldName, builder);
            builder.addMethod(method);
        }
    }

    MethodSpec buildMethod(TypeElement source, ExecutableElement item, TypeElement fieldRef, @Nullable String fieldName, TypeSpec.Builder builder) {
        return null;
    }

    boolean methodSelector(Element element) {
        Set<String> set = element.getAnnotationMirrors().stream().map(item -> item.getAnnotationType().asElement().toString()).collect(Collectors.toSet());
        return set.contains("javax.ws.rs.PATCH") || set.contains("javax.ws.rs.POST") || set.contains("javax.ws.rs.GET") || set.contains("javax.ws.rs.DELETE") || set.contains("javax.ws.rs.PATH") || set.contains("javax.ws.rs.PUT");
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE - 1;
    }
}
