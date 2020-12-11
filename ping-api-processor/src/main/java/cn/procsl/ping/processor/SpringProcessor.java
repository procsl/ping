package cn.procsl.ping.processor;

import cn.procsl.ping.processor.utils.CodeUtils;
import cn.procsl.ping.processor.utils.NamingUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.processing.Generated;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.*;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AutoService(Processor.class)
@SuppressWarnings("unused")
public class SpringProcessor extends AbstractConfigurableProcessor {


    final static protected Set<Class<? extends Annotation>> annotations = Stream.of(Path.class, POST.class, PUT.class, GET.class, DELETE.class, PATCH.class).collect(Collectors.toUnmodifiableSet());
    final static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (roundEnv.processingOver()) {
            return false;
        }

        if (!isInitialized()) {
            return false;
        }

        // 根据每个接口生成springmvc接口
        roundEnv.getElementsAnnotatedWithAny(SpringProcessor.annotations)
            .stream()
            .filter(item -> item instanceof ExecutableElement)
            .distinct()
            .map(item -> (ExecutableElement) item)
            .collect(Collectors.groupingBy(Element::getEnclosingElement))
            .forEach(this::toGeneratorSpringMVC);

        return true;
    }

    protected void toGeneratorSpringMVC(Element element, List<ExecutableElement> methods) {
        if (!(element instanceof TypeElement)) {
            return;
        }
        TypeElement type = (TypeElement) element;

        // 创建接口文件对象
        TypeSpec.Builder builder = this.createType(type);

        // 添加注解
        this.convertToGenerated(builder);
        this.convertToController(builder);
        this.convertToRequestMapping(type, builder);
        this.convertToLombok(builder);

        // 添加 属性
        this.addTypeFiled(type, builder);

        // 转换方法
        for (ExecutableElement method : methods) {
            this.convertMethod(method, builder);
        }
        TypeSpec tmp = builder.build();
        String packageName = this.getPackageName(element);
        this.writeToFile(tmp, packageName);
    }

    void convertMethod(ExecutableElement method, TypeSpec.Builder builder) {
        String name = method.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(name);

        methodBuilder.addModifiers(Modifier.PUBLIC);

        methodBuilder.returns(ClassName.get(method.getReturnType()));

        this.addMethodParams(method, methodBuilder);

        this.convertToMethodAnnotations(method, methodBuilder);

        this.addThrowable(method, methodBuilder);

        builder.addMethod(methodBuilder.build());
    }

    void addThrowable(ExecutableElement method, MethodSpec.Builder methodBuilder) {
        for (TypeMirror thrownType : method.getThrownTypes()) {
            methodBuilder.addException(ClassName.get(thrownType));
        }
    }

    void convertToMethodAnnotations(ExecutableElement method, MethodSpec.Builder methodBuilder) {
        requestMappingBuilder(GET.class, GetMapping.class, method, methodBuilder);
        requestMappingBuilder(POST.class, PostMapping.class, method, methodBuilder);
        requestMappingBuilder(DELETE.class, DeleteMapping.class, method, methodBuilder);
        requestMappingBuilder(PUT.class, PutMapping.class, method, methodBuilder);
        requestMappingBuilder(PATCH.class, PatchMapping.class, method, methodBuilder);
        addResponseBody(methodBuilder);
        //TODO 添加其他 注解
    }

    void addResponseBody(MethodSpec.Builder methodBuilder) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(ResponseBody.class);
        methodBuilder.addAnnotation(builder.build());
    }

    void requestMappingBuilder(Class<? extends Annotation> annotationClass,
                               Class<? extends Annotation> targetClass,
                               ExecutableElement method,
                               MethodSpec.Builder methodBuilder) {
        Annotation annotation = method.getAnnotation(annotationClass);
        if (annotation == null) {
            return;
        }
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(targetClass);
        this.requestMappingParameterBuilder(method, annotationBuilder);
        methodBuilder.addAnnotation(annotationBuilder.build());
    }


    void addMethodParams(ExecutableElement method, MethodSpec.Builder methodBuilder) {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        boolean hsaBody = this.hasBody(method);
        List<? extends VariableElement> parameters = method.getParameters();
        if (hsaBody) {
            List<VariableElement> dtoFields = new ArrayList<>(parameters.size());
            List<VariableElement> simple = new ArrayList<>(parameters.size());

            separationParameters(parameters, dtoFields, simple);
            convertToSimpleParameter(simple, parameterSpecs);

            TypeSpec dto = toBuildDTO(method, parameters, dtoFields);

            if (dto != null) {
                String packageName = getPackageName(method.getEnclosingElement());
                writeToFile(dto, packageName);

                ClassName type = ClassName.get(packageName, dto.name);
                ParameterSpec.Builder paramBuilder = ParameterSpec
                    .builder(type, "dto", Modifier.FINAL);
                AnnotationSpec requestBody = AnnotationSpec.builder(RequestBody.class).build();
                paramBuilder.addAnnotation(requestBody);
                methodBuilder.addParameter(paramBuilder.build());
            }

            this.addCodeBlack(method, methodBuilder, simple, dto);
        } else {
            convertToSimpleParameter(parameters, parameterSpecs);
            this.addCodeBlack(method, methodBuilder, parameters);
        }
        methodBuilder.addParameters(parameterSpecs);
    }

    private String getPackageName(Element enclosingElement) {
        String name = enclosingElement.getEnclosingElement().getSimpleName().toString();
        return name + ".spring";
    }

    void writeToFile(TypeSpec dto, String packageName) {
        try {
            JavaFile.builder(packageName, dto).build().writeTo(filer);
        } catch (IOException exception) {
            log.error("写入文件失败!");
        }
    }

    void addCodeBlack(ExecutableElement method, MethodSpec.Builder methodBuilder, List<? extends VariableElement> parameters) {
        // 简单参数直接调用
        String typeName = NamingUtils.lowerCamelCase(method.getEnclosingElement().getSimpleName().toString());
        String methodName = method.getSimpleName().toString();
        List<String> parameterNames = parameters.stream().map(VariableElement::getSimpleName).map(Object::toString).collect(Collectors.toList());
        String template = "return this.$N.$N(" + CodeUtils.convertToTemplate(parameters, "$N") + ");";
        ArrayList<Object> args = new ArrayList<>();
        args.add(typeName);
        args.add(methodName);
        args.addAll(parameterNames);
        methodBuilder.addCode(template, args.toArray());
    }

    void addCodeBlack(ExecutableElement method, MethodSpec.Builder methodBuilder, List<? extends VariableElement> parameters, TypeSpec dto) {
        if (dto == null) {
            addCodeBlack(method, methodBuilder, parameters);
            return;
        }

        int count = method.getParameters().size();
        String typeName = NamingUtils.lowerCamelCase(method.getEnclosingElement().getSimpleName().toString());
        String methodName = method.getSimpleName().toString();
        List<String> parameterNames = parameters
            .stream()
            .map(VariableElement::getSimpleName)
            .map(Object::toString)
            .map(item -> "dto." + "get" + NamingUtils.upperCamelCase(item) + "()")
            .collect(Collectors.toList());
        String template = "return this.$N.$N(" + CodeUtils.convertToTemplate(count, "$N") + ");";
        ArrayList<Object> args = new ArrayList<>();
        args.add(typeName);
        args.add(methodName);
        args.addAll(parameterNames);
        methodBuilder.addCode(template, args.toArray());
    }

    TypeSpec toBuildDTO(ExecutableElement method, List<? extends VariableElement> parameters, List<VariableElement> dtoFields) {
        if (dtoFields.isEmpty()) {
            return null;
        }

        TypeSpec.Builder builder = getDTOBuilder(method, parameters, dtoFields);
        this.convertToGenerated(builder);
        this.convertToLombok2(builder);

        builder.addSuperinterface(ClassName.get(Serializable.class));
        this.convertDTOFields(dtoFields, builder);
        return builder.build();
    }

    void separationParameters(List<? extends VariableElement> parameters, List<VariableElement> dto, List<VariableElement> simple) {
        for (VariableElement parameter : parameters) {
            // 分离 DTO 参数和 方法参数, 如果被 ParamQuery,
            String value = this.getSimpleParameterName(parameter);
            if (value == null) {
                dto.add(parameter);
            } else {
                simple.add(parameter);
            }
        }
    }

    void convertDTOFields(List<VariableElement> dto, TypeSpec.Builder builder) {
        for (VariableElement element : dto) {
            TypeName type = ClassName.get(element.asType());
            String name = element.getSimpleName().toString();
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(type, name, Modifier.PRIVATE);
            // TODO 添加一些注解
            builder.addField(fieldBuilder.build());
        }
    }

    TypeSpec.Builder getDTOBuilder(ExecutableElement method, List<? extends VariableElement> parameters, List<VariableElement> dto) {
        // 如果存在DTO, 创建DTO, 名称为当前的:类+方法+参数个数+名称
        String typeName = method.getEnclosingElement().getSimpleName().toString();
        String methodName = NamingUtils.upperCamelCase(method.getSimpleName().toString());
        int number = parameters.size();
        String parameterName = dto.stream()
            .map(VariableElement::getSimpleName)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .map(NamingUtils::upperCamelCase)
            .collect(Collectors.joining());
        return TypeSpec
            .classBuilder(typeName + methodName + number + parameterName + "DTO").addModifiers(Modifier.PUBLIC);
    }

    void convertToLombok2(TypeSpec.Builder builder) {
        this.convertToLombok(builder);

        AnnotationSpec toString = AnnotationSpec.builder(ToString.class).build();
        builder.addAnnotation(toString);

        AnnotationSpec equalsAndHashCode = AnnotationSpec.builder(EqualsAndHashCode.class).build();
        builder.addAnnotation(equalsAndHashCode);
    }

    String getSimpleParameterName(VariableElement parameter) {
        BeanParam beanParam = parameter.getAnnotation(BeanParam.class);
        if (beanParam != null) {
            return null;
        }
        QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
        if (queryParam != null) {
            return queryParam.value();
        }

        PathParam pathParam = parameter.getAnnotation(PathParam.class);
        if (pathParam != null) {
            return pathParam.value();
        }

        MatrixParam matrixParam = parameter.getAnnotation(MatrixParam.class);
        if (matrixParam != null) {
            return matrixParam.value();
        }

        CookieParam cookieParam = parameter.getAnnotation(CookieParam.class);
        if (cookieParam != null) {
            return cookieParam.value();
        }

        HeaderParam headerParam = parameter.getAnnotation(HeaderParam.class);
        if (headerParam != null) {
            return headerParam.value();
        }
        return null;
    }

    private void convertToSimpleParameter(List<? extends VariableElement> parameters, List<ParameterSpec> parameterSpecs) {
        for (VariableElement parameter : parameters) {

            TypeName type = ParameterizedTypeName.get(parameter.asType());

            @NonNull
            String name = parameter.getSimpleName().toString();

            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, name);

            String defaultValue = "";
            DefaultValue defaultValueAnnotation = parameter.getAnnotation(DefaultValue.class);
            if (defaultValueAnnotation != null) {
                defaultValue = defaultValueAnnotation.value();
            }

            // TODO 还有其他几个注解 参数校验什么的, 直接迁移转换
            boolean hasAnnotation;
            // 注解转换
            hasAnnotation = this.convertQueryParamToRequestParam(parameter, parameterBuilder, defaultValue, false);
            if (!hasAnnotation) {
                hasAnnotation = this.convertPathParamToPathVariable(parameter, parameterBuilder);
            }
            if (!hasAnnotation) {
                hasAnnotation = this.convertMatrixParamToMatrixVariable(parameter, parameterBuilder, defaultValue);
            }
            if (!hasAnnotation) {
                hasAnnotation = this.convertCookieParamToCookieValue(parameter, parameterBuilder, defaultValue);
            }
            if (!hasAnnotation) {
                hasAnnotation = this.convertHeaderParamToRequestHeader(parameter, parameterBuilder, defaultValue);
            }
            if (!hasAnnotation) {
                // 添加默认注解
                this.convertQueryParamToRequestParam(parameter, parameterBuilder, defaultValue, true);
            }
            parameterSpecs.add(parameterBuilder.build());
        }
    }

    boolean convertHeaderParamToRequestHeader(VariableElement parameter,
                                              ParameterSpec.Builder parameterBuilder,
                                              String defaultValue) {
        HeaderParam annotation = parameter.getAnnotation(HeaderParam.class);
        if (annotation == null) {
            return false;
        }
        AnnotationSpec.Builder builder = AnnotationSpec.builder(RequestHeader.class);
        String value = annotation.value();
        builder.addMember("value", "$S", value);
        builder.addMember("defaultValue", "$S", defaultValue);
        parameterBuilder.addAnnotation(builder.build());
        return true;
    }

    boolean convertCookieParamToCookieValue(VariableElement parameter,
                                            ParameterSpec.Builder parameterBuilder,
                                            String defaultValue) {
        CookieParam annotation = parameter.getAnnotation(CookieParam.class);
        if (annotation == null) {
            return false;
        }
        AnnotationSpec.Builder builder = AnnotationSpec.builder(CookieValue.class);
        String value = annotation.value();
        builder.addMember("value", "$S", value);
        builder.addMember("defaultValue", "$S", defaultValue);
        parameterBuilder.addAnnotation(builder.build());
        return true;
    }


    boolean convertMatrixParamToMatrixVariable(VariableElement parameter,
                                               ParameterSpec.Builder parameterBuilder,
                                               String defaultValue) {
        MatrixParam annotation = parameter.getAnnotation(MatrixParam.class);
        if (annotation == null) {
            return false;
        }

        AnnotationSpec.Builder builder = AnnotationSpec.builder(MatrixVariable.class);
        String value = annotation.value();
        builder.addMember("value", "$S", value);
        builder.addMember("defaultValue", "$S", defaultValue);
        parameterBuilder.addAnnotation(builder.build());
        return true;
    }

    boolean convertPathParamToPathVariable(VariableElement parameterizable,
                                           ParameterSpec.Builder parameterBuilder) {
        PathParam annotation = parameterizable.getAnnotation(PathParam.class);
        if (annotation == null) {
            return false;
        }

        AnnotationSpec.Builder builder = AnnotationSpec.builder(PathVariable.class);
        String value = annotation.value();
        builder.addMember("value", "$S", value);
        parameterBuilder.addAnnotation(builder.build());
        return true;
    }

    boolean convertQueryParamToRequestParam(VariableElement parameterizable,
                                            ParameterSpec.Builder parameterBuilder,
                                            String defaultValue, boolean isDefault) {

        if (isDefault) {
            AnnotationSpec.Builder builder = AnnotationSpec.builder(RequestParam.class);
            builder.addMember("required", "false");
            builder.addMember("defaultValue", "$S", defaultValue);
            parameterBuilder.addAnnotation(builder.build());
            return true;
        }

        QueryParam annotation = parameterizable.getAnnotation(QueryParam.class);
        if (annotation == null) {
            return false;
        }

        AnnotationSpec.Builder builder = AnnotationSpec.builder(RequestParam.class);
        String value = annotation.value();
        builder.addMember("value", "$S", value);
        builder.addMember("defaultValue", "$S", defaultValue);
        parameterBuilder.addAnnotation(builder.build());
        return true;
    }

    boolean hasBody(ExecutableElement method) {
        // 对于 POST, PUT, PATCH 需要转换为DTO
        POST post = method.getAnnotation(POST.class);
        if (post != null) {
            return true;
        }

        PATCH patch = method.getAnnotation(PATCH.class);
        if (patch != null) {
            return true;
        }

        PUT put = method.getAnnotation(PUT.class);
        return put != null;
    }

    void convertToLombok(TypeSpec.Builder builder) {
        AnnotationSpec getter = AnnotationSpec.builder(Getter.class).build();
        builder.addAnnotation(getter);

        AnnotationSpec setter = AnnotationSpec.builder(Setter.class).build();
        builder.addAnnotation(setter);

        AnnotationSpec noArgs = AnnotationSpec.builder(NoArgsConstructor.class).build();
        builder.addAnnotation(noArgs);

        AnnotationSpec allArgs = AnnotationSpec.builder(AllArgsConstructor.class).build();
        builder.addAnnotation(allArgs);
    }

    void addTypeFiled(TypeElement type, TypeSpec.Builder builder) {
        ClassName field = ClassName.get(type);
        String name = NamingUtils.lowerCamelCase(type.getSimpleName().toString());
        FieldSpec.Builder fieldAnnotation = FieldSpec.builder(field, name, Modifier.PRIVATE);
        this.convertAutowired(fieldAnnotation);
        builder.addField(fieldAnnotation.build());
    }

    private void convertAutowired(FieldSpec.Builder fieldAnnotation) {
        AnnotationSpec.Builder autowired = AnnotationSpec
            .builder(Autowired.class);
        fieldAnnotation.addAnnotation(autowired.build());
    }

    TypeSpec.Builder createType(TypeElement type) {

        // 首先获取名称
        String name = type.getSimpleName().toString();
        if (name.endsWith("Controller")) {
            name = name.replaceAll("Service$", "Controller");
        } else {
            name = name + "Controller";
        }

        return TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
    }

    void convertToController(TypeSpec.Builder builder) {
        AnnotationSpec.Builder controller = AnnotationSpec.builder(Controller.class);
        builder.addAnnotation(controller.build());
    }


    void convertToGenerated(TypeSpec.Builder builder) {
        AnnotationSpec.Builder annotation = AnnotationSpec.builder(Generated.class)
            .addMember("value", "{$S}", this.getClass().getName())
            .addMember("date", "$S", fmt.format(new Date()));
        builder.addAnnotation(annotation.build());
    }

    void convertToRequestMapping(TypeElement type, TypeSpec.Builder builder) {
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec
            .builder(RequestMapping.class);
        requestMappingParameterBuilder(type, annotationBuilder);
        builder.addAnnotation(annotationBuilder.build());
    }

    void requestMappingParameterBuilder(Element type, AnnotationSpec.Builder annotationBuilder) {
        {
            Path path = type.getAnnotation(Path.class);
            if (path != null) {
                String pathName = path.value();
                annotationBuilder.addMember("path", "{$S}", pathName.isEmpty() ? "/" : pathName);
            }
        }

        {
            Produces produces = type.getAnnotation(Produces.class);
            if (produces != null) {
                String[] product = produces.value();
                annotationBuilder.addMember("produces", "{" + CodeUtils.convertToTemplate(product, "$S") + "}", (Object[]) product);
            }
        }

        {
            Consumes consumes = type.getAnnotation(Consumes.class);
            if (consumes != null) {
                String[] consume = consumes.value();
                annotationBuilder.addMember("consumes", "{" + CodeUtils.convertToTemplate(consume, "$S") + "}", (Object[]) consume);
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Path.class.getName());
    }


}
