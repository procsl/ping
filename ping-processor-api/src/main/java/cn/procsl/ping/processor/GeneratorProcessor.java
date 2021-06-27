package cn.procsl.ping.processor;

import cn.procsl.ping.processor.generator.FieldAnnotationSpecBuilder;
import cn.procsl.ping.processor.generator.MethodAnnotationSpecBuilder;
import cn.procsl.ping.processor.generator.ParameterAnnotationSpecBuilder;
import cn.procsl.ping.processor.generator.TypeAnnotationSpecBuilder;
import cn.procsl.ping.processor.utils.CodeUtils;
import cn.procsl.ping.processor.utils.NamingUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractConfigurableProcessor implements ProcessorContext {

    final HashMap<String, Object> attrs = new HashMap<>();

    final ServiceLoader<TypeAnnotationSpecBuilder> typeAnnotationSpecBuilders = ServiceLoader.load(TypeAnnotationSpecBuilder.class, this.getClass().getClassLoader());

    final ServiceLoader<FieldAnnotationSpecBuilder> fieldAnnotationSpecBuilders = ServiceLoader.load(FieldAnnotationSpecBuilder.class, this.getClass().getClassLoader());

    final ServiceLoader<MethodAnnotationSpecBuilder> methodAnnotationSpecBuilders = ServiceLoader.load(MethodAnnotationSpecBuilder.class, this.getClass().getClassLoader());

    final ServiceLoader<ParameterAnnotationSpecBuilder> parameterAnnotationSpecBuilders = ServiceLoader.load(ParameterAnnotationSpecBuilder.class, this.getClass().getClassLoader());

    @Getter
    RoundEnvironment roundEnvironment;

    @Getter
    Set<? extends TypeElement> annotations;

    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        synchronized (attrs) {
            this.roundEnvironment = roundEnv;
            this.annotations = annotations;
        }

        Set<TypeElement> element = this.selector(roundEnv);
        for (TypeElement typeElement : element) {

            String name = this.createClassName(typeElement);

            TypeSpec.Builder builder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);

            for (TypeAnnotationSpecBuilder specBuilder : typeAnnotationSpecBuilders) {
                AnnotationSpec spec = specBuilder.build(this, typeElement);
                if (spec != null) {
                    builder.addAnnotation(spec);
                }
            }

            String fieldName = NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString());
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(typeElement.asType()), fieldName, Modifier.PROTECTED);
            for (FieldAnnotationSpecBuilder fieldAnnotationSpecBuilder : this.fieldAnnotationSpecBuilders) {
                AnnotationSpec annotation = fieldAnnotationSpecBuilder.build(this, typeElement);
                if (annotation != null) {
                    fieldSpecBuilder.addAnnotation(annotation);
                }
            }
            builder.addField(fieldSpecBuilder.build());

            typeElement.getEnclosedElements().stream()
                .filter(item -> item instanceof ExecutableElement)
                .filter(item -> (item).getAnnotation(Path.class) != null)
                .map(item -> (ExecutableElement) item)
                .forEach(item -> this.buildMethod(builder, item, fieldName));

            String packageName = typeElement.getEnclosingElement().toString() + ".gen";
            JavaFile java = JavaFile
                .builder(packageName, builder.build())
                .addFileComment("这是自动生成的代码，请勿修改").build();
            java.writeTo(this.getFiler());
        }
    }

    private void buildMethod(TypeSpec.Builder builder, ExecutableElement item, String fieldName) {
        String methodName = item.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC);
        methodBuilder.returns(TypeName.get(item.getReturnType()));

        for (TypeMirror typeMirror : item.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(typeMirror));
        }

        for (MethodAnnotationSpecBuilder specBuilder : this.methodAnnotationSpecBuilders) {
            AnnotationSpec methodAnnotation = specBuilder.build(this, item);
            if (methodAnnotation != null) {
                methodBuilder.addAnnotation(methodAnnotation);
            }
        }

        List<String> params = new ArrayList<>(item.getParameters().size());
        params.add(fieldName);
        params.add(methodName);
        for (VariableElement parameter : item.getParameters()) {
            String simpleName = NamingUtils.lowerCamelCase(parameter.getSimpleName().toString());
            params.add(simpleName);

            TypeMirror type = parameter.asType();
            TypeName typeName = TypeName.get(type);
            if (type.getKind().isPrimitive() && (!typeName.isBoxedPrimitive())) {
                typeName = typeName.box();
            }

            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(typeName, simpleName, Modifier.FINAL);

            for (ParameterAnnotationSpecBuilder parameterAnnotationSpecBuilder : this.parameterAnnotationSpecBuilders) {
                AnnotationSpec parameterAnnotation = parameterAnnotationSpecBuilder.build(this, parameter);
                if (parameterAnnotation != null) {
                    parameterBuilder.addAnnotation(parameterAnnotation);
                }
            }

            methodBuilder.addParameter(parameterBuilder.build());
        }

        String template = String.format(" return this.$N.$N(%s); ", CodeUtils.convertToTemplate(params.size() - 2, "$N"));
        methodBuilder.addCode(template, params.toArray());
        builder.addMethod(methodBuilder.build());
    }

    @Override
    public ProcessingEnvironment getProcessingEnvironment() {
        return this.processingEnv;
    }

    @Override
    public Messager getMessager() {
        return this.messager;
    }

    @Override
    public Filer getFiler() {
        return filer;
    }

    @Override
    public synchronized void setAttribute(@NonNull String key, Object attr) {
        this.attrs.put(key, attr);
    }

    @Override
    public synchronized Object getAttribute(@NonNull String key) {
        return this.attrs.get(key);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Path.class.getName());
    }

    Set<TypeElement> selector(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Path.class);
        return elements
            .stream()
            .filter(item -> item instanceof ExecutableElement)
            .map(item -> (ExecutableElement) item)
            .map(Element::getEnclosingElement)
            .filter(item -> item instanceof TypeElement)
            .map(item -> (TypeElement) item)
            .collect(Collectors.toSet());
    }

    String createClassName(TypeElement element) {
        String name = element.getSimpleName().toString();
        if (name.endsWith("Service")) {
            name = name.replaceAll("Service$", "Controller");
        } else {
            name = name + "Controller";
        }
        return name;
    }


}
