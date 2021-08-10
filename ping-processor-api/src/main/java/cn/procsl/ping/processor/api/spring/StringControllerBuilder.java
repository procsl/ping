package cn.procsl.ping.processor.api.spring;

import cn.procsl.ping.processor.api.AbstractGeneratorBuilder;
import cn.procsl.ping.processor.api.GeneratorBuilder;
import cn.procsl.ping.processor.api.annotation.HttpStatus;
import cn.procsl.ping.processor.api.syntax.VariableDTOElement;
import cn.procsl.ping.processor.api.utils.CodeUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import lombok.SneakyThrows;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.GET;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(GeneratorBuilder.class)
public class StringControllerBuilder extends AbstractGeneratorBuilder {


    final AnnotationSpec validate = AnnotationSpec.builder(ClassName.get("org.springframework.validation.annotation", "Validated")).build();

    final AnnotationSpec restController = AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation", "RestController")).build();

    final AnnotationSpec indexed = AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Indexed")).build();

    final ClassName simpleTypeWrapper = ClassName.get("cn.procsl.ping.web", "SimpleTypeWrapper");

    final AnnotationSpec transaction = AnnotationSpec
        .builder(ClassName.get("org.springframework.transaction.annotation", "Transactional"))
        .addMember("rollbackFor", "$T.class", Exception.class).build();

    final AnnotationSpec readOnlyTransaction = transaction.toBuilder()
        .addMember("readOnly", "$N", "true").build();

    final ClassName autowired = ClassName.get("org.springframework.beans.factory.annotation", "Autowired");

    final static DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CHINA);

    final ClassName responseStatusName = ClassName.get("org.springframework.web.bind.annotation", "ResponseStatus");

    final RequestMappingAnnotation request = new RequestMappingAnnotation();

    @Override
    public boolean support(String type) {
        return "CONTROLLER".equals(type);
    }

    @Override
    public void typeAnnotation(String type, Element element, TypeSpec.Builder spec) {
        String prefix = this.context.getConfig("processor.api.prefix");
        spec.addAnnotation(request.builder(prefix, element));
        spec.addAnnotation(validate);
        spec.addAnnotation(restController);
        spec.addAnnotation(indexed);

        AnnotationSpec generator = AnnotationSpec
            .builder(Generated.class)
            .addMember("value", "{$S}", format.format(new Date()))
            .build();
        spec.addAnnotation(generator);

    }

    @Override
    public void fieldAnnotation(String type, Element element, FieldSpec.Builder spec) {
        AnnotationSpec annotationSpec = AnnotationSpec.builder(autowired).addMember("required", "true").build();
        spec.addAnnotation(annotationSpec);
    }

    @Override
    public void methodAnnotation(String type, Element element, MethodSpec.Builder spec) {
        AnnotationSpec annotation = this.request.builder(element);
        spec.addAnnotation(annotation);

        if (!(element instanceof ExecutableElement)) {
            return;
        }

        if (element.getAnnotation(GET.class) != null) {
            spec.addAnnotation(readOnlyTransaction);
        } else {
            spec.addAnnotation(transaction);
        }

        AnnotationSpec.Builder statusAnnotation = AnnotationSpec.builder(responseStatusName);
        HttpStatus httpStatus = element.getAnnotation(HttpStatus.class);
        if (httpStatus != null) {
            statusAnnotation.addMember("code", "$S", String.valueOf(httpStatus.code()));
        }

        if (((ExecutableElement) element).getReturnType().getKind().toString().equals("VOID")) {
            statusAnnotation.addMember("code", "$N", "org.springframework.http.HttpStatus.NO_CONTENT");
            statusAnnotation.addMember("reason", "$S", "no content");
            spec.addAnnotation(statusAnnotation.build());
        }
    }

    @Override
    public void parameterAnnotation(String type, Element element, ParameterSpec.Builder spec) {
        if (element instanceof VariableDTOElement) {
            spec.addAnnotation(this.validate);
            return;
        }

        Set<AnnotationSpec> annotations = element.getAnnotationMirrors()
            .stream()
            .filter(item -> item.getAnnotationType().toString().startsWith("javax.validation"))
            .map(AnnotationSpec::get)
            .collect(Collectors.toSet());
        spec.addAnnotations(annotations);
    }

    @Override
    public TypeName returnType(String type, ExecutableElement element) {

        TypeMirror returned = element.getReturnType();
        if (returned.getKind().toString().equals("VOID")) {
            return TypeName.get(element.getReturnType());
        }

        if (CodeUtils.hasNeedWrapper(element.getReturnType())) {
            TypeName returnType = TypeName.get(returned);
            return ParameterizedTypeName.get(simpleTypeWrapper, returnType);
        }

        boolean bool = returned.toString().startsWith(Optional.class.getName()) && returned instanceof DeclaredType;
        if (!bool) {
            return TypeName.get(returned);
        }

        List<? extends TypeMirror> args = ((DeclaredType) returned).getTypeArguments();
        if (args.isEmpty()) {
            return TypeName.get(returned);
        }

        TypeMirror argType = args.get(0);
        return ClassName.get(argType);
//
//        ReturnedTypeBuilder builder = new ReturnedTypeBuilder(argType, this.context);
//        TypeName typeName = hasToDTO(argType) ? builder.toBuildReturnType() : ClassName.get(argType);
//
//        if (CodeUtils.hasNeedWrapper(argType)) {
//            return ParameterizedTypeName.get(simpleTypeWrapper, typeName);
//        }
//        // 其他
//        return typeName;
    }


    boolean hasToDTO(TypeMirror argType) {
        Element element = this.context.getProcessingEnvironment().getTypeUtils().asElement(argType);
        Set<String> set = element.getAnnotationMirrors().stream().map(Object::toString).filter(item -> item.startsWith("@javax.persistence")).collect(Collectors.toSet());
        return !set.isEmpty();
    }

    @SneakyThrows
    @Override
    public CodeBlock returnCodeBlack(String type, ExecutableElement element, CodeBlock preStatement) {

        TypeMirror returned = element.getReturnType();
        if (returned.getKind().toString().equals("VOID")) {
            return CodeBlock.builder().add(preStatement).build();
        }

        CodeBlock.Builder start = CodeBlock.builder().add("\n").add(preStatement);
        if (CodeUtils.hasNeedWrapper(returned)) {
            TypeName returnType = TypeName.get(returned);
            ParameterizedTypeName parameter = ParameterizedTypeName.get(simpleTypeWrapper, returnType);
            start.add("\nreturn new $T(returnObject);", parameter).add("\n");
            return start.build();
        }

        boolean bool = returned.toString().startsWith(Optional.class.getName()) && returned instanceof DeclaredType;
        if (!bool) {
            return start.add("\nreturn returnObject;").add("\n").build();
        }

        List<? extends TypeMirror> args = ((DeclaredType) returned).getTypeArguments();
        if (args.isEmpty()) {
            return start.add("\nreturn returnObject;").add("\n").build();
        }

        TypeMirror argType = args.get(0);
        String tmp = " if (returnObject.isEmpty()) {\n\t  throw new $T($N, $S, $S); \n\t} \n $T option = returnObject.get(); \n";

        ClassName exp = ClassName.get("cn.procsl.ping.business.exception", "BusinessException");
        CodeBlock notFound = CodeBlock.builder().add(tmp, exp, "404", "H001", "Not Found", TypeName.get(argType)).build();

        start.add(notFound);

        if (hasToDTO(argType)) {
            start.add(" option.toString(); ");
        }

        // 是否需要包装
        if (CodeUtils.hasNeedWrapper(argType)) {
            return start.add("\nreturn new $T(option);", simpleTypeWrapper).add("\n").build();
        }

        // 是否需要将entity转换成dto
//        if (hasToDTO(argType)) {
//            ReturnedTypeBuilder builder = new ReturnedTypeBuilder(argType, this.context);
//            ClassName returnedType = builder.toBuildReturnType();
//
//            TypeSpec dto = builder.createReturnDTO();
//            JavaFile.builder(returnedType.packageName(), dto).build().writeTo(this.context.getFiler());
//
//            start.add(builder.getCaller());
//
//            return start.add("\nreturn returnDto;").add("\n").build();
//        }

        return start.add("\nreturn option;").add("\n").build();
    }
}
