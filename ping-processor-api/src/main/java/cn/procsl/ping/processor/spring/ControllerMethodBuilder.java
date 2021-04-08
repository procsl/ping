package cn.procsl.ping.processor.spring;

import cn.procsl.ping.processor.common.StringCodeModel;
import cn.procsl.ping.processor.common.TypeMirrorNamingModel;
import cn.procsl.ping.processor.model.*;
import cn.procsl.ping.processor.utils.CodeUtils;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.ws.rs.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class ControllerMethodBuilder {

    final TypeElement typeElement;

    private List<MethodModel> list;

    public Collection<MethodModel> getMethods() {
        if (list == null) {
            this.list = typeElement
                .getEnclosedElements()
                .stream()
                .filter(item -> item instanceof ExecutableElement)
                .map(item -> (ExecutableElement) item)
                .filter(item -> item.getModifiers().contains(Modifier.PUBLIC))
                .filter(item -> CodeUtils.existsAny(item, Path.class, GET.class,
                    POST.class, DELETE.class, PUT.class, PATCH.class))
                .distinct()
                .map(this::methodBuilder)
                .collect(Collectors.toList());
        }

        return null;
    }

    protected MethodModel methodBuilder(ExecutableElement executableElement) {

        return null;
    }


    @RequiredArgsConstructor
    static class SpringMethod implements MethodModel {

        final TypeElement typeElement;

        final ExecutableElement executableElement;

        @Override
        public String getMethodName() {
            return executableElement.getSimpleName().toString();
        }

        @Override
        public Collection<AnnotationModel> getAnnotations() {
            return null;
        }

        @Override
        public Collection<Modifier> getModifiers() {
            return Collections.singletonList(Modifier.PUBLIC);
        }

        @Override
        public Collection<FieldModel> getArguments() {
            return null;
        }

        @Override
        public Collection<NamingModel> getThrowable() {
            return null;
        }

        @Override
        public NamingModel getReturn() {
            return new TypeMirrorNamingModel(this.executableElement.getReturnType());
        }

        @Override
        public CodeModel getBody() {
            return new StringCodeModel("$N", "{}");
        }

        @Override
        public String getSignature() {
            return this.executableElement.toString();
        }
    }

}
