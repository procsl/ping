package cn.procsl.ping.processor.restful.utils;


import com.squareup.javapoet.ClassName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassUtils {


    public static boolean exists(@NonNull String name) {

        try {
            Class.forName(name);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isSimpleParameter(Element variableElement) {
        Set<String> set = variableElement.getAnnotationMirrors().stream().map(item -> item.getAnnotationType().asElement().toString()).collect(Collectors.toSet());
        return set.contains("javax.ws.rs.QueryParam")
            || set.contains("javax.ws.rs.MatrixParam")
            || set.contains("javax.ws.rs.HeaderParam")
            || set.contains("javax.ws.rs.CookieParam");
    }


    public static boolean isSimpleRequest(Element executableElement) {
        POST post = executableElement.getAnnotation(POST.class);
        if (post != null) {
            return false;
        }
        PUT put = executableElement.getAnnotation(PUT.class);
        if (put != null) {
            return false;
        }
        PATCH patch = executableElement.getAnnotation(PATCH.class);
        return patch == null;
    }

    // 创建返回值DTO
    public static ClassName toBuildReturnType(TypeMirror mirror, Types types) {
        Element element = types.asElement(mirror);
        String name = element.getSimpleName().toString() + "DTO";
        String packageName = element.getEnclosingElement().toString() + ".controller.returned";
        return ClassName.get(packageName, name);

    }

    public static boolean isPersistenceEntity(Element element) {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        return isPersistenceEntity(mirrors);
    }

    public static boolean isPersistenceEntity(Collection<? extends AnnotationMirror> mirrors) {
        for (AnnotationMirror annotationMirror : mirrors) {
            String item = annotationMirror.toString();
            if (item.startsWith("@javax.persistence")) {
                return true;
            }
        }
        return false;
    }


}
