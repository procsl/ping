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
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class GeneratorProcessor extends AbstractConfigurableProcessor implements ProcessorContext {

    final HashMap<String, Object> attrs = new HashMap<>();

    static final String CONTROLLER = "CONTROLLER";

    static final String DTO = "DTO";

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

        for (AnnotationSpecBuilder specBuilder : annotationSpecBuilders) {
            specBuilder.build(this, item, methodBuilder, CONTROLLER);
        }

        ParameterConstructor constructor = new ParameterConstructor(this, methodName, fieldName, item);
        methodBuilder.addParameters(constructor.buildParameters());

//        String template = String.format(" return this.$N.$N(%s); ", CodeUtils.convertToTemplate(params.size() - 2, "$N"));
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
        if (name.endsWith("Service")) {
            name = name.replaceAll("Service$", "Controller");
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

        HashMap<Integer, VariableElement> dtoFields = new HashMap<>();

        HashMap<Integer, VariableElement> params = new HashMap<>();

        boolean simpleRequest;

        int size;

        public ParameterConstructor(GeneratorProcessor processor, String methodName, String fieldName, @NonNull ExecutableElement executableElement) {
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
            this.dtoName = "NameDTO" + dtoFields.size() + "" + params.size();
            this.size = parameters.size();
        }

        boolean isSimpleRequest(ExecutableElement executableElement) {
            return false;
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
            return false;
        }

        public List<ParameterSpec> buildParameters() {
            int initialCapacity = this.params.size() + this.dtoFields.size();
            if (initialCapacity == 0) {
                return Collections.emptyList();
            }

            ArrayList<ParameterSpec> result = new ArrayList<>(initialCapacity);
            buildParamSpec(result);
            if (!dtoFields.isEmpty()) {
                TypeSpec.Builder dto = TypeSpec.classBuilder(this.dtoName);
                for (AnnotationSpecBuilder specBuilder : processor.annotationSpecBuilders) {
                    specBuilder.build(processor, this.executableElement, dto, DTO);
                }
                dtoFields.forEach((k, v) -> {
                    TypeName type = TypeName.get(v.asType());
                    FieldSpec.Builder fieldBuilder = FieldSpec.builder(type, v.getSimpleName().toString(), Modifier.PROTECTED);

                    for (AnnotationSpecBuilder specBuilder : processor.annotationSpecBuilders) {
                        specBuilder.build(processor, v, fieldBuilder, DTO);
                    }

                    dto.addField(fieldBuilder.build());
                });
            }
            return Collections.unmodifiableList(result);
        }

        void buildParamSpec(ArrayList<ParameterSpec> result) {
            for (Map.Entry<Integer, VariableElement> entry : params.entrySet()) {
                Integer k = entry.getKey();
                VariableElement v = entry.getValue();
                buildSpec(result, k, v);
            }
        }

        void buildSpec(ArrayList<ParameterSpec> result, Integer k, VariableElement v) {
            String simpleName = NamingUtils.lowerCamelCase(v.getSimpleName().toString());
            TypeName typeName = toBoxed(v);
            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(typeName, simpleName, Modifier.FINAL);
            for (AnnotationSpecBuilder specBuilder : processor.annotationSpecBuilders) {
                specBuilder.build(processor, v, parameterBuilder, CONTROLLER);
            }
            result.add(k, parameterBuilder.build());
        }

        public CodeBlock buildCaller() {

            retured = ""
            if (size == 0) {
                return
            }

            ArrayList<String> templates = new ArrayList();
            for (int i = 0; i < this.size; i++) {
                VariableElement sip = params.get(i);
                if (sip != null) {
                    templates.add(i, sip.getSimpleName().toString());
                } else {
                    VariableElement field = dtoFields.get(i);
                    String name = field.getSimpleName().toString();
                    String callStr = String.format("%s.get%s()", this.fieldName, NamingUtils.upperCamelCase(name));
                    templates.add(i, callStr);
                }
            }

            return null;
        }


    }

}
