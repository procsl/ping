package cn.procsl.ping.processor.api.syntax;

import lombok.Getter;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.*;

public class ParameterVariableElement implements VariableElement {

    @Getter
    final ExecutableElement enclosingElement;

    final Map<Integer, VariableElement> elements;

    final String packageName;

    ParameterTypeMirrorModel type;

    @Getter
    Set<Modifier> modifiers;

    @Getter
    Name simpleName;

    String constant;

    public ParameterVariableElement(ExecutableElement enclosingElement,
                                    Map<Integer, javax.lang.model.element.VariableElement> elements,
                                    String packageName,
                                    String name) {
        this.enclosingElement = enclosingElement;
        this.elements = elements;
        this.packageName = packageName;
        this.modifiers = Collections.singleton(Modifier.PUBLIC);
        this.simpleName = new ParameterNameModel(name);
        this.constant = packageName + simpleName;
        this.type = new ParameterTypeMirrorModel(constant);
    }

    @Override
    public Object getConstantValue() {
        return constant;
    }

    @Override
    public TypeMirror asType() {
        return type;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.TYPE_PARAMETER;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return new ArrayList<>(elements.values());
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
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitVariable(this, p);
    }
}
