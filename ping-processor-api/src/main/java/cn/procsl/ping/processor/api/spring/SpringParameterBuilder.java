package cn.procsl.ping.processor.api.spring;

import cn.procsl.ping.processor.api.AbstractGeneratorBuilder;
import cn.procsl.ping.processor.api.GeneratorBuilder;
import cn.procsl.ping.processor.api.syntax.VariableDTOElement;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.ws.rs.*;

@AutoService(GeneratorBuilder.class)
public class SpringParameterBuilder extends AbstractGeneratorBuilder {

    final static String requestParam = "org.springframework.web.bind.annotation.RequestParam";
    final static String responseBody = "org.springframework.web.bind.annotation.RequestBody";
    final static String pathVariable = "org.springframework.web.bind.annotation.PathVariable";
    final static String cookieValue = "org.springframework.web.bind.annotation.CookieValue";
    final static String requestHeader = "org.springframework.web.bind.annotation.RequestHeader";
    final static String matrixVariable = "org.springframework.web.bind.annotation.MatrixVariable";


    @Override
    public void parameterAnnotation(String type, Element source, ParameterSpec.Builder target) {

        if (source == null) {
            context.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.WARNING, "找不到VariableElement");
            return;
        }

        if (source instanceof VariableDTOElement) {
            AnnotationSpec spec = AnnotationSpec.builder(ClassName.bestGuess(responseBody)).build();
            target.addAnnotation(spec);
            return;
        }

        DefaultValue defaultValue = source.getAnnotation(DefaultValue.class);
        boolean bool = false;
        {
            QueryParam query = source.getAnnotation(QueryParam.class);
            if (query != null) {
                AnnotationSpec.Builder spec = AnnotationSpec
                    .builder(ClassName.bestGuess(requestParam))
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
                    .builder(ClassName.bestGuess(pathVariable))
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
                    .builder(ClassName.bestGuess(requestHeader))
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
                    .builder(ClassName.bestGuess(cookieValue))
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
                    .builder(ClassName.bestGuess(matrixVariable))
                    .addMember("name", matrix.value());
                if (defaultValue != null) {
                    spec.addMember("defaultValue", "$S", defaultValue.value());
                }
                target.addAnnotation(spec.build());
                bool = true;
            }
        }

        // default 如果是 java or javax打头
        if (!bool) {
            String name = source.toString();
            if (name.startsWith("java.") || name.startsWith("javax.")) {
                AnnotationSpec.Builder spec = AnnotationSpec
                    .builder(ClassName.bestGuess(requestParam));
                if (defaultValue != null) {
                    spec.addMember("defaultValue", "$S", defaultValue.value());
                }
                target.addAnnotation(spec.build());
            } else {
                AnnotationSpec spec = AnnotationSpec.builder(ClassName.bestGuess(responseBody)).build();
                target.addAnnotation(spec);
            }

        }

    }


    @Override
    public boolean support(String type) {
        return "CONTROLLER".equals(type);
    }
}
