package cn.procsl.ping.processor;

import cn.procsl.ping.processor.builder.AnnotationSpecBuilder;
import cn.procsl.ping.processor.utils.NamingUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractConfigurableProcessor implements ProcessorContext {

    final HashMap<String, Object> attrs = new HashMap<>();

    static final String CONTROLLER = "CONTROLLER";

    static final String DTO = "DTO";

    static final String SERVICE = "Service";

    final ServiceLoader<AnnotationSpecBuilder> annotationSpecBuilders = ServiceLoader.load(AnnotationSpecBuilder.class, this.getClass().getClassLoader());

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

            for (AnnotationSpecBuilder specBuilder : annotationSpecBuilders) {
                specBuilder.build(this, typeElement, builder, CONTROLLER);
            }

            String fieldName = NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString());
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(typeElement.asType()), fieldName, Modifier.PROTECTED);
            for (AnnotationSpecBuilder specBuilder : annotationSpecBuilders) {
                specBuilder.build(this, typeElement, fieldSpecBuilder, CONTROLLER);
            }
            builder.addField(fieldSpecBuilder.build());

            List<ExecutableElement> list = typeElement.getEnclosedElements().stream()
                .filter(item -> item instanceof ExecutableElement)
                .filter(item -> (item).getAnnotation(Path.class) != null)
                .map(item -> (ExecutableElement) item)
                .collect(Collectors.toList());
            for (ExecutableElement item : list) {
                this.buildMethod(builder, item, fieldName);
            }

            String packageName = typeElement.getEnclosingElement().toString() + ".gen";
            JavaFile java = JavaFile
                .builder(packageName, builder.build())
                .addFileComment("这是自动生成的代码，请勿修改").build();
            java.writeTo(this.getFiler());
        }
    }

    private void buildMethod(TypeSpec.Builder builder, ExecutableElement item, String fieldName) throws IOException {
        String methodName = item.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC);
        methodBuilder.returns(TypeName.get(item.getReturnType()));

        for (TypeMirror typeMirror : item.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(typeMirror));
        }

        for (AnnotationSpecBuilder specBuilder : annotationSpecBuilders) {
            specBuilder.build(this, item, methodBuilder, CONTROLLER);
        }

        ParameterConstructor constructor = new ParameterConstructor(this, methodName, fieldName, item);
        methodBuilder.addParameters(constructor.buildParameters());

        methodBuilder.addCode(constructor.buildCaller());
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
        if (name.endsWith(SERVICE)) {
            name = name.replaceAll(SERVICE + "$", "Controller");
        } else {
            name = name + "Controller";
        }
        return name;
    }

    static final protected class ParameterConstructor {

        final ExecutableElement executableElement;

        final GeneratorProcessor processor;

        final String methodName;

        final String fieldName;

        final String dtoName;

        final HashMap<Integer, VariableElement> dtoFields = new HashMap<>();

        final HashMap<Integer, VariableElement> params = new HashMap<>();

        final boolean simpleRequest;

        final int size;

        final String dtoPackage;

        TypeSpec dtoType;

        public ParameterConstructor(GeneratorProcessor processor, String methodName, String fieldName, @NonNull ExecutableElement executableElement) throws IOException {
            this.executableElement = executableElement;
            this.processor = processor;
            this.methodName = methodName;
            this.fieldName = fieldName;
            this.simpleRequest = isSimpleRequest(executableElement);

            List<? extends VariableElement> parameters = executableElement.getParameters();

            for (int i = 0; i < parameters.size(); i++) {
                if (isSimpleParameter(parameters.get(i))) {
                    params.put(i, parameters.get(i));
                } else {
                    dtoFields.put(i, parameters.get(i));
                }
            }
            this.dtoName = NamingUtils.upperCamelCase(methodName) + dtoFields.size() + params.size() + "DTO";
            this.size = parameters.size();
            this.dtoPackage = getPackage();

            if (!dtoFields.isEmpty()) {
                TypeSpec.Builder dto = TypeSpec.classBuilder(this.dtoName);
                dto.addModifiers(Modifier.PUBLIC);
                for (AnnotationSpecBuilder specBuilder : processor.annotationSpecBuilders) {
                    specBuilder.build(processor, this.executableElement, dto, DTO);
                }
                dtoFields.forEach((k, v) -> {
                    TypeName type = toBoxed(v);
                    FieldSpec.Builder fieldBuilder = FieldSpec.builder(type, v.getSimpleName().toString(), Modifier.PROTECTED);

                    for (AnnotationSpecBuilder specBuilder : processor.annotationSpecBuilders) {
                        specBuilder.build(processor, v, fieldBuilder, DTO);
                    }

                    dto.addField(fieldBuilder.build());
                });
                this.dtoType = dto.build();
                JavaFile javaFile = JavaFile.builder(this.dtoPackage, this.dtoType).build();
                javaFile.writeTo(this.processor.getFiler());
            }

        }

        private String getPackage() {
            Element type = executableElement.getEnclosingElement();
            String packageName = type.getEnclosingElement().toString();
            String serviceName = type.getSimpleName().toString();
            String businessName = serviceName.replaceAll(SERVICE + "$", "").toLowerCase(Locale.ROOT);
            return String.format("%s.gen.dto.%s", packageName, businessName);
        }

        boolean isSimpleRequest(ExecutableElement executableElement) {
            POST post = executableElement.getAnnotation(POST.class);
            if (post != null) {
                return false;
            }
            PUT put = executableElement.getAnnotation(PUT.class);
            if (put != null) {
                return false;
            }
            PATCH patch = executableElement.getAnnotation(PATCH.class);
            return patch == null;
        }

        TypeName toBoxed(VariableElement parameter) {
            TypeMirror type = parameter.asType();
            TypeName typeName = TypeName.get(type);
            if (type.getKind().isPrimitive() && (!typeName.isBoxedPrimitive())) {
                typeName = typeName.box();
            }
            return typeName;
        }

        boolean isSimpleParameter(VariableElement variableElement) {
            Set<String> set = variableElement.getAnnotationMirrors().stream().map(item -> item.getAnnotationType().asElement().toString()).collect(Collectors.toSet());
            return set.contains("javax.ws.rs.QueryParam")
                || set.contains("javax.ws.rs.MatrixParam")
                || set.contains("javax.ws.rs.HeaderParam")
                || set.contains("javax.ws.rs.CookieParam");
        }

        public List<ParameterSpec> buildParameters() {
            if (size == 0) {
                return Collections.emptyList();
            }

            ArrayList<ParameterSpec> result = new ArrayList<>(size);
            buildParamSpec(result);
            if (!dtoFields.isEmpty()) {
                ClassName clazz = ClassName.get(this.dtoPackage, this.dtoName);
                ParameterSpec.Builder dtoBuilder = ParameterSpec.builder(clazz, NamingUtils.lowerCamelCase(this.dtoName), Modifier.FINAL);
                result.add(dtoBuilder.build());
            }
            return result;
        }

        void buildParamSpec(ArrayList<ParameterSpec> result) {
            for (Map.Entry<Integer, VariableElement> entry : params.entrySet()) {
                VariableElement v = entry.getValue();
                buildSpec(result, v);
            }
        }

        void buildSpec(ArrayList<ParameterSpec> result, VariableElement v) {
            String simpleName = NamingUtils.lowerCamelCase(v.getSimpleName().toString());
            TypeName typeName = toBoxed(v);
            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(typeName, simpleName, Modifier.FINAL);
            for (AnnotationSpecBuilder specBuilder : processor.annotationSpecBuilders) {
                specBuilder.build(processor, v, parameterBuilder, CONTROLLER);
            }
            result.add(parameterBuilder.build());
        }

        public CodeBlock buildCaller() {

            String returned = "return this.%s.%s(%s);\n";
            if (size == 0) {
                return CodeBlock.builder().add(String.format(returned, fieldName, methodName, "")).build();
            }

            String[] templates = new String[size];
            for (int i = 0; i < this.size; i++) {
                VariableElement sip = params.get(i);
                if (sip != null) {
                    templates[i] = sip.getSimpleName().toString();
                } else {
                    VariableElement field = dtoFields.get(i);
                    String name = field.getSimpleName().toString();
                    String callStr = String.format("%s.get%s()", NamingUtils.lowerCamelCase(this.dtoName), NamingUtils.upperCamelCase(name));
                    templates[i] = callStr;
                }
            }

            String args = String.join(",", templates);
            return CodeBlock.builder().add(String.format(returned, fieldName, methodName, args)).build();
        }

    }

}
