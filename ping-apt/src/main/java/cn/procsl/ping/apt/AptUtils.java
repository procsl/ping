package cn.procsl.ping.apt;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.Collection;

import static cn.procsl.ping.apt.SimpleElementVisitor.ofVisitType;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;

public final class AptUtils {


    public static boolean isAvailable(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static AnnotationMirror findAnnotation(Elements utils, Collection<? extends AnnotationMirror> mirrors, String typeName) {
        if (mirrors.isEmpty()) {
            return null;
        }

        Name typed = utils.getName(typeName);
        var vt = ofVisitType((e, p) -> typed.equals(e.getQualifiedName()));
        for (AnnotationMirror mirror : mirrors) {
            DeclaredType type = mirror.getAnnotationType();
            Element element = type.asElement();
            Boolean bool = element.accept(vt, null);
            if (bool != null && bool) {
                return mirror;
            }
        }
        return null;
    }

    public static AnnotationValue findAnnotationValue(Elements utils, AnnotationMirror repo, String name) {

        Name named = utils.getName(name);

        var values = repo.getElementValues();
        for (ExecutableElement k : values.keySet()) {
            Name simpleName = k.getSimpleName();
            if (!simpleName.equals(named)) {
                continue;
            }
            return values.get(k);
        }
        return null;
    }


    public static void writer(Messager messager, Filer filer, String packageName, TypeSpec typeSpec) throws IOException {
        // java 源文件表示
        JavaFile file = JavaFile.builder(packageName, typeSpec).build();

        // 写入class
        messager.printMessage(NOTE, "Write java file to: " + packageName + "." + typeSpec.name);
        try {
            file.writeTo(filer);
        } catch (Exception e) {
            messager.printMessage(WARNING,
                " Write java file error: [" + packageName + "." + typeSpec.name + "]" + e.getMessage());
        }
    }


}
