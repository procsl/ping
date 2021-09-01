package cn.procsl.ping.processor.api.spring;

import cn.procsl.ping.processor.api.AbstractAnnotationVisitor;
import cn.procsl.ping.processor.api.AnnotationVisitor;
import cn.procsl.ping.processor.api.syntax.ParameterVariableElement;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.ws.rs.*;

@AutoService(AnnotationVisitor.class)
public class SpringParameterVisitor extends AbstractAnnotationVisitor {

    final static String packageName = "org.springframework.web.bind.annotation";
    final static String requestParam = "RequestParam";
    final static String responseBody = "RequestBody";
    final static String pathVariable = "PathVariable";
    final static String cookieValue = "CookieValue";
    final static String requestHeader = "RequestHeader";
    final static String matrixVariable = "MatrixVariable";


    @Override
    public void parameterVisitor(Element source, ParameterSpec.Builder target) {

        if (source == null) {
            context.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.WARNING, "找不到VariableElement");
            return;
        }

        if (source instanceof ParameterVariableElement) {
            AnnotationSpec spec = AnnotationSpec.builder(ClassName.get(packageName, responseBody)).build();
            target.addAnnotation(spec);
            return;
        }

        DefaultValue defaultValue = source.getAnnotation(DefaultValue.class);
        boolean bool = false;
        {
            QueryParam query = source.getAnnotation(QueryParam.class);
            if (query != null) {
                AnnotationSpec.Builder spec = AnnotationSpec
                    .builder(ClassName.get(packageName, requestParam))
                    .addMember("name", "$S", query.value());
                if (defaultValue != null) {
                    spec.addMember("defaultValue", "$S", defaultValue.value());
                }
                target.addAnnotation(spec.build());
                bool = true;
            }
        }

        {
            PathParam path = source.getAnnotation(PathParam.class);
            if (path != null) {
                AnnotationSpec spec = AnnotationSpec
                    .builder(ClassName.get(packageName, pathVariable))
                    .addMember("name", "$S", path.value())
                    .build();
                target.addAnnotation(spec);
                bool = true;
            }
        }

        {
            HeaderParam header = source.getAnnotation(HeaderParam.class);
            if (header != null) {
                AnnotationSpec.Builder spec = AnnotationSpec
                    .builder(ClassName.get(packageName, requestHeader))
                    .addMember("name", "$S", header.value());
                if (defaultValue != null) {
                    spec.addMember("defaultValue", "$S", defaultValue.value());
                }
                target.addAnnotation(spec.build());
                bool = true;
            }
        }

        {
            CookieParam cookie = source.getAnnotation(CookieParam.class);
            if (cookie != null) {
                AnnotationSpec.Builder spec = AnnotationSpec
                    .builder(ClassName.get(packageName, cookieValue))
                    .addMember("name", "$S", cookie.value());
                if (defaultValue != null) {
                    spec.addMember("defaultValue", "$S", defaultValue.value());
                }
                target.addAnnotation(spec.build());
                bool = true;
            }
        }

        {
            MatrixParam matrix = source.getAnnotation(MatrixParam.class);
            if (matrix != null) {
                AnnotationSpec.Builder spec = AnnotationSpec
                    .builder(ClassName.get(packageName, matrixVariable))
                    .addMember("name", matrix.value());
                if (defaultValue != null) {
                    spec.addMember("defaultValue", "$S", defaultValue.value());
                }
                target.addAnnotation(spec.build());
                bool = true;
            }
        }

        // default 如果是 java or javax打头
        if (bool) {
            return;
        }
        String name = source.toString();
        if (name.startsWith("java.") || name.startsWith("javax.")) {
            AnnotationSpec.Builder spec = AnnotationSpec
                .builder(ClassName.get(packageName, requestParam));
            if (defaultValue != null) {
                spec.addMember("defaultValue", "$S", defaultValue.value());
            }
            target.addAnnotation(spec.build());
        }

    }

    @Override
    public SupportType support() {
        return SupportType.CONTROLLER;
    }
}
