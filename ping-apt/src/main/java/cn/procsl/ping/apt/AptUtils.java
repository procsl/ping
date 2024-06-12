package cn.procsl.ping.apt;

import com.google.auto.common.MoreElements;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import static cn.procsl.ping.apt.SimpleElementVisitor.*;
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

    public static AnnotationValue findAnnotationValueOrDefaultValue(Elements utils, AnnotationMirror repo, String name) {
        Name named = utils.getName(name);
        var values = repo.getElementValues();

        for (ExecutableElement k : values.keySet()) {
            Name simpleName = k.getSimpleName();
            if (!simpleName.equals(named)) {
                continue;
            }

            AnnotationValue v = values.get(k);
            do {
                if (v == null) {
                    break;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitAnnotation((e, s) -> e), null) != null) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisit((e, s) -> e), null) != null) {
                    return v;
                }
                String str = v.accept(SimpleAnnotationValueVisitor.ofVisitString((e, s) -> e), null);
                if (str != null && !str.isEmpty()) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitType((e, s) -> e), null) != null) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitBoolean((e, s) -> e), null) != null) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitByte((e, s) -> e), null) != null) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitChar((e, s) -> e), null) != null) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitDouble((e, s) -> e), null) != null) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitFloat((e, s) -> e), null) != null) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitInt((e, s) -> e), null) != null) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitLong((e, s) -> e), null) != null) {
                    return v;
                }
                var array = v.accept(SimpleAnnotationValueVisitor.ofVisitArray((e, s) -> e), null);
                if (array != null && !array.isEmpty()) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitEnumConstant((e, s) -> e), null) != null) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitShort((e, s) -> e), null) != null) {
                    return v;
                }
                if (v.accept(SimpleAnnotationValueVisitor.ofVisitUnknown((e, s) -> e), null) != null) {
                    return v;
                }
            } while (false);
            return k.getDefaultValue();
        }


        TypeElement tp = repo.getAnnotationType().asElement().accept(ofVisitType((e, s) -> e), null);
        var self = ofVisitExecutable((e, s) -> e);
        Optional<ExecutableElement> result = tp.getEnclosedElements()
            .stream()
            .map((item) -> item.accept(self, null))
            .filter(Objects::nonNull)
            .filter(item -> item.getSimpleName().equals(named))
            .findFirst();

        return result.map(ExecutableElement::getDefaultValue).orElse(null);
    }

    public static void writer(Messager messager, Filer filer, String packageName, TypeSpec typeSpec) {
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
