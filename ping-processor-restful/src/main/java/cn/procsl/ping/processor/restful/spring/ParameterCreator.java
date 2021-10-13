package cn.procsl.ping.processor.restful.spring;

import cn.procsl.ping.processor.AnnotationVisitor;
import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.restful.AnnotationVisitorLoader;
import cn.procsl.ping.processor.restful.model.ParameterVariableElement;
import cn.procsl.ping.processor.restful.utils.ClassUtils;
import cn.procsl.ping.processor.restful.utils.NamingUtils;
import com.squareup.javapoet.*;
import lombok.NonNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.*;

class ParameterCreator {

    final private ExecutableElement executable;

    final private ProcessorContext context;

    final private String methodName;

    final private String fieldName;

    final private String dtoName;

    final private HashMap<Integer, VariableElement> dtoFields = new HashMap<>();

    final private HashMap<Integer, VariableElement> params = new HashMap<>();

    final private boolean simpleRequest;

    final private int size;

    final private String dtoPackage;

    final private boolean createDTO;

    private final AnnotationVisitor parameter;

    private final AnnotationVisitor returned;

    private TypeSpec dtoType;

    private AnnotationVisitor controller;

    public ParameterCreator(ProcessorContext context, String methodName, String fieldName, @NonNull ExecutableElement executableElement) throws IOException {
        this.executable = executableElement;
        this.context = context;
        this.methodName = methodName;
        this.fieldName = fieldName;
        this.simpleRequest = ClassUtils.isSimpleRequest(executableElement);
        this.controller = new AnnotationVisitorLoader(context, "CONTROLLER");
        this.parameter = new AnnotationVisitorLoader(context, "CONTROLLER_PARAMETER");
        this.returned = new AnnotationVisitorLoader(context, "CONTROLLER_RETURNED");

        List<? extends VariableElement> parameters = executableElement.getParameters();

        for (int i = 0; i < parameters.size(); i++) {
            if (simpleRequest) {
                params.put(i, parameters.get(i));
                continue;
            }
            if (ClassUtils.isSimpleParameter(parameters.get(i))) {
                params.put(i, parameters.get(i));
            } else {
                dtoFields.put(i, parameters.get(i));
            }
        }

        this.dtoName = NamingUtils.upperCamelCase(methodName) + "DTO";
        this.size = parameters.size();
        this.dtoPackage = getPackage();
        this.createDTO = (simpleRequest || dtoFields.isEmpty());

        if (createDTO) {
            return;
        }
        TypeSpec.Builder dto = TypeSpec.classBuilder(this.dtoName);
        dto.addModifiers(Modifier.PUBLIC);
        parameter.visitor(executableElement, dto);

        dtoFields.forEach((k, v) -> {
            TypeName type = toWrapper(v);
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(type, v.getSimpleName().toString(), Modifier.PROTECTED);
            parameter.visitor(v, fieldBuilder);
            dto.addField(fieldBuilder.build());
        });
        this.dtoType = dto.build();
        JavaFile javaFile = JavaFile.builder(this.dtoPackage, this.dtoType).build();
        javaFile.writeTo(this.context.getFiler());

    }

    private String getPackage() {
        Element type = executable.getEnclosingElement();
        String packageName = type.getEnclosingElement().toString();
//        String serviceName = type.getSimpleName().toString();
//        String businessName = serviceName.replaceAll("Application", "").replaceAll(SERVICE + "$", "").toLowerCase(Locale.ROOT);
        return String.format("%s", packageName);
    }

    private TypeName toWrapper(javax.lang.model.element.VariableElement parameter) {
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

        Types util = context.getProcessingEnvironment().getTypeUtils();
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


    public List<ParameterSpec> creatorParameters() {
        ParameterSpec spec = this.buildHttpResponse();

        if (size == 0) {
            return Collections.singletonList(spec);
        }

        ArrayList<ParameterSpec> result = new ArrayList<>(size);
        buildParamSpec(result);
        if (this.createDTO) {
            result.add(spec);
            return result;
        }
        ClassName clazz = ClassName.get(this.dtoPackage, this.dtoName);
        ParameterSpec.Builder dtoBuilder = ParameterSpec.builder(clazz, NamingUtils.lowerCamelCase(this.dtoName), Modifier.FINAL);
        controller.visitor(new ParameterVariableElement(executable, dtoFields, this.dtoPackage, this.dtoName), dtoBuilder);

        result.add(dtoBuilder.build());
        result.add(spec);
        return result;
    }

    private ParameterSpec buildHttpResponse() {
        ClassName response = ClassName.get("javax.servlet.http", "HttpServletResponse");
        return ParameterSpec.builder(response, "response", Modifier.FINAL).build();
    }

    private void buildParamSpec(ArrayList<ParameterSpec> result) {
        for (Map.Entry<Integer, javax.lang.model.element.VariableElement> entry : params.entrySet()) {
            javax.lang.model.element.VariableElement v = entry.getValue();
            buildSpec(result, v);
        }
    }

    private void buildSpec(ArrayList<ParameterSpec> result, javax.lang.model.element.VariableElement v) {
        String simpleName = NamingUtils.lowerCamelCase(v.getSimpleName().toString());
        TypeName typeName = toWrapper(v);
        ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(typeName, simpleName, Modifier.FINAL);
        controller.visitor(v, parameterBuilder);

        result.add(parameterBuilder.build());
    }

    public CodeBlock creatorCaller() {

        String returned;
        if (executable.getReturnType().toString().equals("void")) {
            returned = "this.%s.%s(%s);\n";
        } else {
            returned = "$T returnObject =  this.%s.%s(%s);\n";
        }

        if (size == 0) {
            String caller = String.format(returned, fieldName, methodName, "");
            return toWrapper(caller, executable.getReturnType());
        }

        String[] templates = new String[size];
        for (int i = 0; i < this.size; i++) {
            javax.lang.model.element.VariableElement sip = params.get(i);
            if (sip != null) {
                templates[i] = sip.getSimpleName().toString();
            } else {
                javax.lang.model.element.VariableElement field = dtoFields.get(i);
                String name = field.getSimpleName().toString();
                String callStr = String.format("%s.get%s()", NamingUtils.lowerCamelCase(this.dtoName), NamingUtils.upperCamelCase(name));
                templates[i] = callStr;
            }
        }

        String args = String.join(",", templates);
        String caller = String.format(returned, fieldName, methodName, args);
        return toWrapper(caller, executable.getReturnType());
    }

    private CodeBlock toWrapper(String caller, TypeMirror mirror) {
        if (!caller.contains("=")) {
            return CodeBlock.builder().add(caller).build();
        }
        return CodeBlock.builder().add(caller, TypeName.get(mirror)).build();
    }
}
