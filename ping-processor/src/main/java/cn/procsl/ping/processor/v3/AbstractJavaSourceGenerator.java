package cn.procsl.ping.processor.v3;

import com.squareup.javapoet.*;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractJavaSourceGenerator implements JavaSourceGenerator {

    final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public final void generated(Collection<TypeDescriptor> descriptors, ProcessorEnvironment environment) throws IOException {

        for (TypeDescriptor descriptor : descriptors) {

            TypeSpec.Builder typeBuilder = toBuildType(descriptor);

            buildFields(descriptor, typeBuilder, environment);

            buildMethods(descriptor, typeBuilder, environment);

            JavaFile javaFile = JavaFile.builder(descriptor.getPackageName(), typeBuilder.build()).build();
            javaFile.writeTo(environment.getFiler());
        }

    }

    void buildMethods(TypeDescriptor descriptor, TypeSpec.Builder typeBuilder, ProcessorEnvironment environment) {
        for (MethodDescriptor method : descriptor.getMethods()) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getMethodName()).addModifiers(Modifier.PUBLIC);

            TypeName type = this.buildMethodReturn(method, environment);
            methodBuilder.returns(type);

            for (TypeNameDescriptor exception : method.getThrows()) {
                TypeName exceptionType = ClassName.get(exception.getPackageName(), exception.getClassName());
                methodBuilder.addException(exceptionType);
            }

            Collection<MethodParameterDescriptor> items = method.getParameterDescriptor();

            List<ParameterSpec> parameters = items.stream()
                .sorted(Comparator.comparingInt(MethodParameterDescriptor::getIndex))
                .map(item -> this.buildMethodParameter(item, environment))
                .collect(Collectors.toList());

            methodBuilder.addParameters(parameters);

            CodeBlock code = this.buildMethodBody(method, environment);
            methodBuilder.addCode(code);

            typeBuilder.addMethod(methodBuilder.build());
        }
    }

    CodeBlock buildMethodBody(MethodDescriptor method, ProcessorEnvironment environment) {
        SourceCodeDescriptor body = method.getBody();
        return CodeBlock.of(body.getCallSourceCode());
    }

    ParameterSpec buildMethodParameter(MethodParameterDescriptor item, ProcessorEnvironment environment) {
        String name = item.getParameterName();
        Element target = item.getTarget();
        TypeName type;
        if (target != null) {
            type = TypeName.get(target.asType());
        } else {
            type = ClassName.get(item.getPackageName(), item.getClassName());
        }
        return ParameterSpec.builder(type, name, Modifier.PUBLIC, Modifier.FINAL).build();
    }

    TypeName buildMethodReturn(MethodDescriptor method, ProcessorEnvironment environment) {
        MethodReturnValueDescriptor returns = method.getReturnDescriptor();
        return ClassName.get(returns.getPackageName(), returns.getClassName());
    }

    TypeSpec.Builder toBuildType(TypeDescriptor descriptor) {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(descriptor.getClassName()).addModifiers(Modifier.PUBLIC);

        Date now = new Date();
        String name = String.format("%s.%s", AbstractJavaSourceGenerator.class.getPackageName(), AbstractJavaSourceGenerator.class.getName());
        AnnotationSpec annotation = AnnotationSpec.builder(Generated.class).addMember("value", "$S", name).addMember("date", "$S", SIMPLE_DATE_FORMAT.format(now)).build();
        typeBuilder.addAnnotation(annotation);

        return typeBuilder;
    }

    void buildFields(TypeDescriptor descriptor, TypeSpec.Builder typeBuilder, ProcessorEnvironment environment) {
        for (FieldDescriptor field : descriptor.getFields()) {

            TypeName type;

            Element target = field.getTarget();
            if (target != null) {
                type = TypeName.get(target.asType());
            } else {
                type = ClassName.get(field.getPackageName(), field.getClassName());
            }

            FieldSpec.Builder fieldBuilder = FieldSpec.builder(type, field.getFieldName()).addModifiers(Modifier.PROTECTED);

            typeBuilder.addField(fieldBuilder.build());
        }
    }

//    abstract <R, P> TargetVisitor<R, P> getVisitor();

}
