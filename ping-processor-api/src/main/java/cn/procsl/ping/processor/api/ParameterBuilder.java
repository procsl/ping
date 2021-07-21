package cn.procsl.ping.processor.api;

import cn.procsl.ping.processor.api.syntax.VariableDTOElement;
import cn.procsl.ping.processor.api.utils.NamingUtils;
import com.squareup.javapoet.*;
import lombok.NonNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class ParameterBuilder {

    static final String DTO = "DTO";

    static final String CONTROLLER = "CONTROLLER";

    static final String SERVICE = "Service";

    final ExecutableElement executable;

    final GeneratorProcessor processor;

    final String methodName;

    final String fieldName;

    final String dtoName;

    final HashMap<Integer, VariableElement> dtoFields = new HashMap<>();

    final HashMap<Integer, VariableElement> params = new HashMap<>();

    final boolean simpleRequest;

    final int size;

    final String dtoPackage;

    final boolean createDTO;

    TypeSpec dtoType;

    public ParameterBuilder(GeneratorProcessor processor, String methodName, String fieldName, @NonNull ExecutableElement executableElement) throws IOException {
        this.executable = executableElement;
        this.processor = processor;
        this.methodName = methodName;
        this.fieldName = fieldName;
        this.simpleRequest = isSimpleRequest(executableElement);

        List<? extends VariableElement> parameters = executableElement.getParameters();

        for (int i = 0; i < parameters.size(); i++) {
            if (simpleRequest) {
                params.put(i, parameters.get(i));
                continue;
            }
            if (isSimpleParameter(parameters.get(i))) {
                params.put(i, parameters.get(i));
            } else {
                dtoFields.put(i, parameters.get(i));
            }
        }

        this.dtoName = NamingUtils.upperCamelCase(methodName) + dtoFields.size() + params.size() + "DTO";
        this.size = parameters.size();
        this.dtoPackage = getPackage();
        this.createDTO = (simpleRequest || dtoFields.isEmpty());

        if (createDTO) {
            return;
        }
        TypeSpec.Builder dto = TypeSpec.classBuilder(this.dtoName);
        dto.addModifiers(Modifier.PUBLIC);
        for (GeneratorBuilder specBuilder : processor.dto) {
            specBuilder.typeAnnotation(DTO, executableElement, dto);
        }
        dtoFields.forEach((k, v) -> {
            TypeName type = toWrapper(v);
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(type, v.getSimpleName().toString(), Modifier.PROTECTED);

            for (GeneratorBuilder specBuilder : processor.dto) {
                specBuilder.fieldAnnotation(DTO, v, fieldBuilder);
            }

            dto.addField(fieldBuilder.build());
        });
        this.dtoType = dto.build();
        JavaFile javaFile = JavaFile.builder(this.dtoPackage, this.dtoType).build();
        javaFile.writeTo(this.processor.getFiler());

    }

    private String getPackage() {
        Element type = executable.getEnclosingElement();
        String packageName = type.getEnclosingElement().toString();
        String serviceName = type.getSimpleName().toString();
        String businessName = serviceName.replaceAll("Application", "").replaceAll(SERVICE + "$", "").toLowerCase(Locale.ROOT);
        return String.format("%s.api.%s", packageName, businessName);
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

    TypeName toWrapper(VariableElement parameter) {
        TypeMirror type = parameter.asType();
        TypeName typeName = TypeName.get(type);
        if (type.getKind().isPrimitive() && (!typeName.isBoxedPrimitive())) {
            typeName = typeName.box();
        }

        if (!(typeName instanceof ParameterizedTypeName)) {
            return typeName;
        }

        if (!(type instanceof DeclaredType)) {
            return typeName;
        }

        if (((DeclaredType) type).getTypeArguments().isEmpty()) {
            return typeName;
        }

        Types util = processor.getProcessingEnvironment().getTypeUtils();
        List<? extends TypeMirror> arguments = ((DeclaredType) type).getTypeArguments();
        ArrayList<String> cont = new ArrayList<>();
        for (TypeMirror item : arguments) {
            Element ele = util.asElement(item);
            String simpleName = ele.getSimpleName().toString();
            String annotationStr = item.toString().replaceAll(ele.toString(), "").trim();
            if (!annotationStr.endsWith(")")) {
                annotationStr += "()";
            }
            cont.add(String.format("%s.%s %s", ele.getEnclosingElement().toString(), annotationStr, simpleName));
        }

        ClassName rewType = ((ParameterizedTypeName) typeName).rawType;
        return TypeVariableName.get(String.format("%s.%s<%s>", rewType.packageName(), rewType.simpleName(), String.join(", ", cont)));
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
        if (this.createDTO) {
            return result;
        }
        ClassName clazz = ClassName.get(this.dtoPackage, this.dtoName);
        ParameterSpec.Builder dtoBuilder = ParameterSpec.builder(clazz, NamingUtils.lowerCamelCase(this.dtoName), Modifier.FINAL);
        for (GeneratorBuilder specBuilder : processor.controller) {
            specBuilder.parameterAnnotation(CONTROLLER, new VariableDTOElement(executable, dtoFields, this.dtoPackage, this.dtoName), dtoBuilder);
        }
        result.add(dtoBuilder.build());
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
        TypeName typeName = toWrapper(v);
        ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(typeName, simpleName, Modifier.FINAL);
        for (GeneratorBuilder specBuilder : processor.controller) {
            specBuilder.parameterAnnotation(CONTROLLER, v, parameterBuilder);
        }
        result.add(parameterBuilder.build());
    }

    public CodeBlock buildCaller() {

        String returned;
        if (executable.getReturnType().toString().equals("void")) {
            returned = " this.%s.%s(%s);\n";
        } else {
            returned = "return this.%s.%s(%s);\n";
        }

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
