package cn.procsl.ping.processor.api.utils;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.lang.model.element.Element;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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


}
