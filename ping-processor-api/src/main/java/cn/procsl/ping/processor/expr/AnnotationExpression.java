package cn.procsl.ping.processor.expr;

import cn.procsl.ping.processor.Expression;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

public class AnnotationExpression implements Expression<AnnotationSpec> {


    AnnotationSpec.Builder builder;

    AnnotationMirror annotationMirror;

    Annotation annotation;

    public AnnotationExpression(String packageName, String name) {
        builder = AnnotationSpec.builder(ClassName.get(packageName, name));
    }

    public AnnotationExpression(AnnotationMirror annotationMirror) {
        this.annotationMirror = annotationMirror;
    }

    public AnnotationExpression(Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public AnnotationSpec interpret(@NonNull ProcessingEnvironment environment, @NonNull RoundEnvironment roundEnvironment, @NonNull Element root) {

        if (builder != null) {
            builder.build();
        }

        if (annotationMirror != null) {
            return AnnotationSpec.get(this.annotationMirror);
        }

        if (annotation != null) {
            return AnnotationSpec.get(annotation, true);
        }

        throw new IllegalArgumentException("错误的参数!");
    }
}
