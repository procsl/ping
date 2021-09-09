package cn.procsl.ping.processor.restful.model;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class ParameterTypeMirrorModel implements TypeMirror {

    final String name;

    public ParameterTypeMirrorModel(String name) {
        this.name = name;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.TYPEVAR;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visit(this, p);
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return Collections.emptyList();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        A[] arr = (A[]) Collections.emptyList().toArray();
        return arr;
    }

    @Override
    public String toString() {
        return name;
    }
}
