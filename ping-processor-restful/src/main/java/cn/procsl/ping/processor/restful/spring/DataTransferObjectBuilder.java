package cn.procsl.ping.processor.restful.spring;

import cn.procsl.ping.processor.AnnotationVisitor;
import cn.procsl.ping.processor.restful.utils.NamingUtils;
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

import static cn.procsl.ping.processor.restful.utils.ClassUtils.isPersistenceEntity;
import static cn.procsl.ping.processor.restful.utils.ClassUtils.toBuildReturnType;

public class DataTransferObjectBuilder {

    final TypeElement OPTIONAL_TYPE;

    final TypeElement ITERABLE_TYPE;

    final TypeElement SET_TYPE;

    final TypeElement LIST_TYPE;

    final TypeElement MAP_TYPE;

    final TypeSpec.Builder masterBuilder;

    final TypeElement returnElement;

    final Set<VariableElement> simpleFieldElements = new HashSet<>();

    final HashMap<TypeElement, VariableElement> collectionFieldElements = new HashMap<>();

    final Set<VariableElement> entityElements = new HashSet<>();

    final DeclaredType idMirror;

    final VariableElement idElement;

    final Types utils;

    final Elements elementUtils;

    final ClassName returnedType;

    final CodeBlock.Builder codeBlack;

    final TypeMirror returnMirror;

    final String methodName;

    final TypeElement COLLECTION_TYPE;

    final Set<String> SIMPLE_TYPE = new HashSet<>(Arrays.asList(
        String.class.getName(),
        BigInteger.class.getName(),
        BigDecimal.class.getName(),
        Date.class.getName(),
        java.sql.Date.class.getName(),
        Character.class.getName()
    ));

    final AnnotationVisitor visitor;

    public DataTransferObjectBuilder(Elements elementUtils,
                                     Types utils,
                                     AnnotationVisitor visitor, TypeMirror argType,
                                     String argName) {

        this.utils = utils;
        this.visitor = visitor;
        this.elementUtils = elementUtils;
        this.returnedType = toBuildReturnType(argType, utils);
        this.masterBuilder = TypeSpec
            .classBuilder(returnedType)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(Serializable.class);
        this.returnMirror = argType;

        this.returnElement = (TypeElement) utils.asElement(argType);

        this.COLLECTION_TYPE = elementUtils.getTypeElement(Collection.class.getName());
        this.MAP_TYPE = elementUtils.getTypeElement(Map.class.getName());
        this.LIST_TYPE = elementUtils.getTypeElement(List.class.getName());
        this.SET_TYPE = elementUtils.getTypeElement(Set.class.getName());
        this.ITERABLE_TYPE = elementUtils.getTypeElement(Iterable.class.getName());
        this.OPTIONAL_TYPE = elementUtils.getTypeElement(Optional.class.getName());

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

        builderMethodAndField(fieldElements);

        // build convert
        this.methodName = "convertTo";
        buildConvertMethod();
        this.codeBlack = CodeBlock.builder().add("\n$T dto =  $T.$N($N);\n", returnedType, returnedType, methodName, argName);
    }

    private void builderMethodAndField(Set<VariableElement> fieldElements) {
        for (VariableElement fieldElement : fieldElements) {

            if (isSimpleType(fieldElement)) {
                this.simpleFieldElements.add(fieldElement);
                this.buildFieldAndGetterSetter(fieldElement);
                continue;
            }

            if (isSubType(fieldElement, this.SET_TYPE)) {
                this.collectionFieldElements.put(this.SET_TYPE, fieldElement);
                continue;
            }

            if (isSubType(fieldElement, this.LIST_TYPE)) {
                this.collectionFieldElements.put(this.LIST_TYPE, fieldElement);
                continue;
            }

            if (isSubType(fieldElement, this.MAP_TYPE)) {
                this.collectionFieldElements.put(this.MAP_TYPE, fieldElement);
                continue;
            }

            if (isSubType(fieldElement, this.COLLECTION_TYPE)) {
                this.collectionFieldElements.put(this.COLLECTION_TYPE, fieldElement);
                continue;
            }

            if (isSubType(fieldElement, this.ITERABLE_TYPE)) {
                this.collectionFieldElements.put(this.ITERABLE_TYPE, fieldElement);
                continue;
            }

            if (isSubType(fieldElement, this.OPTIONAL_TYPE)) {
                this.collectionFieldElements.put(this.OPTIONAL_TYPE, fieldElement);
                continue;
            }

            if (isPersistenceEntity(fieldElement)) {
                this.entityElements.add(fieldElement);
            }

        }
    }

    private boolean isSubType(VariableElement fieldElement, TypeElement element) {
        TypeMirror fieldMirror = fieldElement.asType();
        TypeMirror erasureMirror = this.utils.erasure(fieldMirror);
        TypeMirror protoType = this.utils.erasure(element.asType());
        return this.utils.isSubtype(erasureMirror, protoType);
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

    void buildFieldAndGetterSetter(VariableElement fieldElement) {
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

    public CodeBlock getCaller() {
        return this.codeBlack.build();
    }

    public void toWrite(Filer filer) throws IOException {
        JavaFile
            .builder(this.returnedType.packageName(), this.masterBuilder.build()).
            build().writeTo(filer);
    }
}
