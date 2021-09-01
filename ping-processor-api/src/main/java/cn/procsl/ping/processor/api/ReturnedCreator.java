package cn.procsl.ping.processor.api;

import cn.procsl.ping.processor.api.utils.CodeUtils;
import cn.procsl.ping.processor.api.utils.NamingUtils;
import com.squareup.javapoet.*;
import lombok.NonNull;

import javax.annotation.processing.Filer;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

class ReturnedCreator {

    private final static ClassName simpleTypeWrapper = ClassName.get("cn.procsl.ping.web", "SimpleTypeWrapper");
    private final GeneratorProcessor generatorProcessor;
    private final String methodName;
    private final String fieldName;
    private final ExecutableElement methodElement;
    private final CodeBlock buildCaller;
    private final Types utils;
    private final Elements elements;
    private final TypeName returnType;
    private final TypeMirror returned;
    private final AnnotationVisitor returnAnnotationVisitor;

    public ReturnedCreator(GeneratorProcessor generatorProcessor, String methodName, String fieldName, ExecutableElement element, CodeBlock buildCaller) {
        this.generatorProcessor = generatorProcessor;
        this.methodName = methodName;
        this.fieldName = fieldName;
        this.methodElement = element;
        this.buildCaller = buildCaller;
        this.returned = element.getReturnType();
        this.utils = this.generatorProcessor.getProcessingEnvironment().getTypeUtils();
        this.elements = this.generatorProcessor.getProcessingEnvironment().getElementUtils();
        this.returnAnnotationVisitor = new AnnotationVisitorLoader(generatorProcessor, AnnotationVisitor.SupportType.CONTROLLER_RETURNED);
        this.returnType = init();
    }

    public TypeName createReturnedType() {
        return this.returnType;
    }

    private TypeName init() {

        if (returned.getKind().toString().equals("VOID")) {
            return TypeName.get(returned);
        }

        if (CodeUtils.hasNeedWrapper(returned)) {
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

        TypeName typeName = hasToDTO(argType) ? this.toBuildReturnType(argType) : ClassName.get(argType);

        if (CodeUtils.hasNeedWrapper(argType)) {
            return ParameterizedTypeName.get(simpleTypeWrapper, typeName);
        }

        // 其他
        return typeName;
    }

    // 创建返回值DTO
    public ClassName toBuildReturnType(TypeMirror mirror) {
        Element element = this.generatorProcessor.getProcessingEnvironment().getTypeUtils().asElement(mirror);
        String name = element.getSimpleName().toString() + "DTO";
        String packageName = element.getEnclosingElement().toString() + ".returned";
        return ClassName.get(packageName, name);
    }


    public CodeBlock createCodeBlack() throws IOException {

        if (returned.getKind().toString().equals("VOID")) {
            return CodeBlock.builder().add(this.buildCaller).build();
        }

        CodeBlock.Builder start = CodeBlock.builder().add("\n").add(this.buildCaller);
        if (CodeUtils.hasNeedWrapper(returned)) {
//            TypeName returnType = TypeName.get(returned);
//            ParameterizedTypeName parameter = ParameterizedTypeName.get(simpleTypeWrapper, returnType);
            start.add("\nreturn new $T(returnObject);", this.returnType).add("\n");
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

        // 是否需要包装
        if (CodeUtils.hasNeedWrapper(argType)) {
            return start.add("\nreturn new $T(option);", simpleTypeWrapper).add("\n").build();
        }

        // 是否需要将entity转换成dto
        if (!hasToDTO(argType)) {
            return start.add("\nreturn option;").add("\n").build();
        }

        ClassName returnedType = toBuildReturnType(argType);

        String argName = "option";

        ReturnedDTOBuilder builder = new ReturnedDTOBuilder(elements, utils, this.returnAnnotationVisitor, argType, returnedType, argName);

        builder.toWrite(this.generatorProcessor.getFiler());

        return start.add(builder.getCaller()).add("\nreturn dto;\n").build();
    }


    private boolean hasToDTO(TypeMirror argType) {
        Element element = utils.asElement(argType);
        Set<String> set = element.getAnnotationMirrors()
            .stream()
            .map(Object::toString)
            .filter(item -> item.startsWith("@javax.persistence"))
            .collect(Collectors.toSet());
        return !set.isEmpty();
    }

    private final static class ReturnedDTOBuilder {

        final TypeElement MAP_ELEMENT_TYPE;

        final TypeSpec.Builder masterBuilder;

        final TypeElement returnElement;

        final Set<VariableElement> simpleFieldElements = new HashSet<>();

        final Set<VariableElement> collectionFieldElements = new HashSet<>();

        final Set<VariableElement> entityElements = new HashSet<>();

        final DeclaredType idMirror;

        final VariableElement idElement;

        final Types utils;

        final Elements elementUtils;

        final ClassName returnedType;

        final CodeBlock.Builder codeBlack;

        final TypeMirror returnMirror;

        final String methodName;

        final TypeElement COLLECTION_ELEMENT_TYPE;

        final Set<String> SIMPLE_TYPE = new HashSet<>(Arrays.asList(
            String.class.getName(),
            BigInteger.class.getName(),
            BigDecimal.class.getName(),
            Date.class.getName(),
            java.sql.Date.class.getName(),
            Character.class.getName()
        ));

        private final AnnotationVisitor visitor;

        public ReturnedDTOBuilder(Elements elementUtils, Types utils, AnnotationVisitor visitor, TypeMirror argType,
                                  ClassName returnedType, String argName) {

            this.utils = utils;
            this.visitor = visitor;
            this.elementUtils = elementUtils;
            this.masterBuilder = TypeSpec
                .classBuilder(returnedType)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(Serializable.class);
            this.returnedType = returnedType;
            this.returnMirror = argType;

            this.returnElement = (TypeElement) utils.asElement(argType);
            this.COLLECTION_ELEMENT_TYPE = elementUtils.getTypeElement(Collection.class.getName());
            this.MAP_ELEMENT_TYPE = elementUtils.getTypeElement(Map.class.getName());

            visitor.typeVisitor(returnElement, masterBuilder);

            List<? extends Element> elements = returnElement.getEnclosedElements();
            Set<VariableElement> fieldElements = elements
                .stream()
                .filter(item -> item instanceof VariableElement)
                .map(item -> (VariableElement) item)
                .collect(Collectors.toSet());

            this.idElement = this.getMarkIdVariableElement(fieldElements);
            if (this.idElement == null) {
                this.idMirror = this.findId(this.returnElement);
            } else {
                fieldElements.remove(idElement);
                this.idMirror = null;
            }

            for (VariableElement fieldElement : fieldElements) {

                if (isSimpleType(fieldElement)) {
                    this.simpleFieldElements.add(fieldElement);
                    continue;
                }

                if (isCollectionType(fieldElement)) {
                    this.collectionFieldElements.add(fieldElement);
                    continue;
                }

                if (isEntityType(fieldElement)) {
                    this.entityElements.add(fieldElement);
                }

            }

            buildFieldAndGetterSetter();

            // build convert
            this.methodName = "convertTo";
            buildConvertMethod();
            this.codeBlack = CodeBlock.builder().add("\n$T dto =  $T.$N($N);\n", returnedType, returnedType, methodName, argName);
        }

        private boolean isSimpleType(VariableElement fieldElement) {
            TypeMirror fieldMirror = fieldElement.asType();
            if (fieldMirror.getKind().isPrimitive()) {
                return true;
            }
            if (this.SIMPLE_TYPE.contains(fieldMirror.toString())) {
                return true;
            }
            // 判断继承了枚举的

            return false;
        }

        private boolean isCollectionType(VariableElement fieldElement) {
            TypeMirror fieldMirror = fieldElement.asType();
            // 判断是否继承了容器接口

            return false;
        }

        private boolean isEntityType(VariableElement fieldElement) {
            // 判断是否存在注解标记
            return false;
        }

        private void buildConvertMethod() {
            MethodSpec.Builder convertMethodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.FINAL, Modifier.STATIC, Modifier.PUBLIC);
            convertMethodBuilder.returns(returnedType);
            //argument entity name
            String argumentName = NamingUtils.lowerCamelCase(this.returnElement.getSimpleName().toString());

            // entity type
            TypeName entityType = TypeName.get(this.returnMirror);
            ParameterSpec.Builder parameter = ParameterSpec.builder(entityType, argumentName, Modifier.FINAL);
            convertMethodBuilder.addParameter(parameter.build());

            // DTO variable name
            String dtoName = NamingUtils.lowerCamelCase(returnedType.simpleName());

            // 构造函数 DTO dto = new DTO();
            convertMethodBuilder.addCode("$T $N = new $T();\n", returnedType, dtoName, returnedType);

            // 赋值
            for (VariableElement fieldElement : this.simpleFieldElements) {
                @NonNull String currentFieldName = fieldElement.getSimpleName().toString();
                convertMethodBuilder.addCode("$N.$N = $N.get$N();\n", dtoName, currentFieldName, argumentName, NamingUtils.upperCamelCase(currentFieldName));
            }

            convertMethodBuilder.addCode("return $N;\n", dtoName);
            this.masterBuilder.addMethod(convertMethodBuilder.build());
        }

        private void buildFieldAndGetterSetter() {
            for (VariableElement fieldElement : simpleFieldElements) {
                String fieldName = fieldElement.getSimpleName().toString();
                TypeName type = TypeName.get(fieldElement.asType());
                FieldSpec.Builder fields = FieldSpec.builder(type, fieldName, Modifier.PUBLIC);

                String upperCamelCaseStr = NamingUtils.upperCamelCase(fieldName);
                String getterStr = String.format("get%s", upperCamelCaseStr);
                MethodSpec.Builder getter = MethodSpec.methodBuilder(getterStr).addModifiers(Modifier.PUBLIC).returns(type).addCode("\nreturn this.$N;\n", fieldName);

                String setterStr = String.format("set%s", upperCamelCaseStr);
                MethodSpec.Builder setter = MethodSpec.methodBuilder(setterStr).addModifiers(Modifier.PUBLIC).returns(TypeName.VOID).addCode("\n this.$N = $N;\n", fieldName, fieldName);

                ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, fieldName, Modifier.FINAL);

                visitor.fieldVisitor(fieldElement, fields);
                visitor.methodVisitor(fieldElement, getter);
                visitor.parameterVisitor(fieldElement, parameterBuilder);
                visitor.methodVisitor(fieldElement, setter);
                setter.addParameter(parameterBuilder.build());
                masterBuilder.addField(fields.build());
                masterBuilder.addMethod(setter.build());
                masterBuilder.addMethod(getter.build());
            }


        }


        private DeclaredType findId(TypeElement returned) {

            if (!(returned.getSuperclass() instanceof DeclaredType)) {
                return null;
            }

            DeclaredType superClass = (DeclaredType) returned.getSuperclass();
            if (superClass == null) {
                return null;
            }

            TypeElement superElement = (TypeElement) utils.asElement(superClass);
            if (!superClass.toString().startsWith("org.springframework.data.jpa.domain.AbstractPersistable")) {
                return this.findId(superElement);
            }

            return superClass;
        }

        private VariableElement getMarkIdVariableElement(Set<VariableElement> elements) {
            for (VariableElement element : elements) {
                List<? extends AnnotationMirror> exist = element
                    .getAnnotationMirrors()
                    .stream()
                    .filter(item -> item.toString().startsWith("@javax.persistence.id"))
                    .collect(Collectors.toList());
                if (!exist.isEmpty()) {
                    return element;
                }
            }
            return null;
        }

        CodeBlock getCaller() {
            return this.codeBlack.build();
        }

        public void toWrite(Filer filer) throws IOException {
            JavaFile.builder(this.returnedType.packageName(), this.masterBuilder.build()).
                build().writeTo(filer);
        }
    }

}
