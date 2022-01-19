package cn.procsl.ping.processor.v3.web.parser;

import cn.procsl.ping.processor.v3.MethodDescriptor;
import cn.procsl.ping.processor.v3.MethodReturnValueDescriptor;
import cn.procsl.ping.processor.v3.ProcessorEnvironment;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

class MethodDescriptorImpl implements MethodDescriptor {

    private final ExecutableElement element;
    private final AnnotationMirror httpMethod;
    private final ParameterDescriptorImpl requestParameter;
    private final MethodReturnValueDescriptor requestReturnDescriptor;

    MethodDescriptorImpl(ExecutableElement element, ProcessorEnvironment environment) {
        this.element = element;
        this.requestParameter = new ParameterDescriptorImpl(this);
        this.requestReturnDescriptor = new ReturnDescriptorImpl(this);

        AnnotationMirror get = environment.findAnnotationMirror(element, GET.class);
        if (get != null) {
            httpMethod = get;
            return;
        }

        AnnotationMirror post = environment.findAnnotationMirror(element, POST.class);
        if (post != null) {
            httpMethod = post;
            return;
        }

        AnnotationMirror delete = environment.findAnnotationMirror(element, DELETE.class);
        if (delete != null) {
            httpMethod = delete;
            return;
        }
        AnnotationMirror put = environment.findAnnotationMirror(element, PUT.class);
        if (put != null) {
            httpMethod = put;
            return;
        }
        httpMethod = environment.findAnnotationMirror(element, PATCH.class);
    }

    @Override
    public String getMethodName() {
        return this.getTargetMethodElement().getSimpleName().toString();
    }

    @Override
    public TypeElement getParentElement() {
        return (TypeElement) element.getEnclosingElement();
    }

    @Override
    public ExecutableElement getTargetMethodElement() {
        return element;
    }

    @Override
    public List<String> getThrows() {
        return this.getTargetMethodElement().getThrownTypes().stream().map(TypeMirror::toString).collect(Collectors.toList());
    }

    @Override
    public String getHttpMethod() {
        if (httpMethod == null) {
            return null;
        }
        return this.httpMethod.getAnnotationType().asElement().getSimpleName().toString();
    }
//
//    @Override
//    public MethodParameterDescriptor getRequestParameterDescriptor() {
//        return this.requestParameter;
//    }
//
//    @Override
//    public MethodReturnValueDescriptor getRequestReturnDescriptor() {
//        return this.requestReturnDescriptor;
//    }

    @Override
    public String[] findConsumes() {
        Consumes consumes = this.findAnnotation(Consumes.class);
        if (consumes != null) {
            return consumes.value();
        }
        return new String[0];
    }

    @Override
    public String[] findProduces() {
        Produces produces = this.findAnnotation(Produces.class);
        if (produces != null) {
            return produces.value();
        }
        return new String[0];
    }

    <T extends Annotation> T findAnnotation(Class<T> clazz) {
        T tmp = element.getAnnotation(clazz);
        if (tmp == null) {
            return this.getParentElement().getAnnotation(clazz);
        }
        return null;
    }

    public boolean isRequest() {
        return this.httpMethod != null;
    }

}
