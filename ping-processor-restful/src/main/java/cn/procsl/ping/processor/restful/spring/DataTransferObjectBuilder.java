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

    final List<DataTransferObjectBuilder> builders = new ArrayList<>();

    final Set<String> SIMPLE_TYPE = new HashSet<>(Arrays.asList(
        String.class.getName(),
        BigInteger.class.getName(),
        BigDecimal.class.getName(),
        Date.class.getName(),
        java.sql.Date.class.getName(),
        Character.class.getName()
    ));

    final AnnotationVisitor visitor;

    final MethodSpec.Builder convertMethodBuilder;
    final String convertArgumentName;
    String convertDTOName;

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

        visitor.visitor(returnElement, masterBuilder);

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

        // build convert
        this.methodName = "convertTo";
        this.codeBlack = CodeBlock.builder().add("\n$T dto =  $T.$N($N);\n", returnedType, returnedType, methodName, argName);

        this.convertMethodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.FINAL, Modifier.STATIC, Modifier.PUBLIC);
        this.convertArgumentName = NamingUtils.lowerCamelCase(this.returnElement.getSimpleName().toString());

        convertMethodBuilder.returns(returnedType);
        //argument entity name

        // entity type
        TypeName entityType = TypeName.get(this.returnMirror);
        ParameterSpec.Builder parameter = ParameterSpec.builder(entityType, convertArgumentName, Modifier.FINAL);
        convertMethodBuilder.addParameter(parameter.build());

        // DTO variable name
        String dtoName = NamingUtils.lowerCamelCase(returnedType.simpleName());

        // 构造函数 DTO dto = new DTO();
        convertMethodBuilder.addCode("$T $N = new $T();\n", returnedType, dtoName, returnedType);

        this.convertDTOName = dtoName;

        builderMethodAndField(fieldElements);

        convertMethodBuilder.addCode("return $N;\n", convertDTOName);
        masterBuilder.addMethod(this.convertMethodBuilder.build());
    }

    private void builderMethodAndField(Set<VariableElement> fieldElements) {
        for (VariableElement fieldElement : fieldElements) {

            if (isSimpleType(fieldElement)) {
                this.simpleFieldElements.add(fieldElement);
                this.buildFieldAndGetterSetter(fieldElement);
                // 赋值
                @NonNull String currentFieldName = fieldElement.getSimpleName().toString();
                convertMethodBuilder.addCode("$N.$N = $N.get$N();\n", this.convertDTOName, currentFieldName, convertArgumentName, NamingUtils.upperCamelCase(currentFieldName));
                continue;
            }

            if (isSubType(fieldElement, this.SET_TYPE)) {
                builderCollections(fieldElement, this.SET_TYPE, HashSet.class, 0);
                continue;
            }

            if (isSubType(fieldElement, this.LIST_TYPE)) {
                builderCollections(fieldElement, this.LIST_TYPE, ArrayList.class, 0);
                continue;
            }

            if (isSubType(fieldElement, this.MAP_TYPE)) {
                this.collectionFieldElements.put(this.MAP_TYPE, fieldElement);
                continue;
            }

            if (isSubType(fieldElement, this.COLLECTION_TYPE)) {
                builderCollections(fieldElement, this.COLLECTION_TYPE, ArrayList.class, 0);
                continue;
            }

            if (isSubType(fieldElement, this.ITERABLE_TYPE)) {
                builderCollections(fieldElement, this.ITERABLE_TYPE, ArrayList.class, 0);
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

    void builderCollections(VariableElement fieldElement, TypeElement type, Class<? extends Iterable> impl, int index) {
        this.collectionFieldElements.put(type, fieldElement);
        TypeMirror mirror = this.getGenerics(fieldElement, index);
        if (mirror == null) {
            return;
        }

        TypeName typeName = TypeName.get(mirror);

        boolean isPersistence = isPersistenceEntity(this.utils.asElement(mirror));
        String fieldName = fieldElement.getSimpleName().toString();

        ParameterizedTypeName collectType = ParameterizedTypeName.get(ClassName.get(type), typeName);
        if (isPersistence) {
            DataTransferObjectBuilder trans = new DataTransferObjectBuilder(this.elementUtils, this.utils, this.visitor, mirror, fieldName);
            this.builders.add(trans);
            typeName = trans.getType();
            collectType = ParameterizedTypeName.get(ClassName.get(type), typeName);
            getterAndSetterMethodBuilder(fieldElement, fieldName, collectType);
        }

        FieldSpec.Builder field = FieldSpec.builder(collectType, fieldName, Modifier.PUBLIC);
        visitor.visitor(fieldElement, field);
        this.masterBuilder.addField(field.build());

        // 创建convert
        String lowName = NamingUtils.lowerCamelCase(type.getSimpleName().toString());

//        type = (TypeElement) this.utils.asElement(this.utils.erasure(type.asType()));

        ParameterizedTypeName t = ParameterizedTypeName.get(ClassName.get(type), typeName);
        this.convertMethodBuilder.addStatement("{\n$T $N = new $T<>()", t, lowName, impl);
        this.convertMethodBuilder.addStatement("$N.$N = $N", this.convertDTOName, fieldName, lowName);
        this.convertMethodBuilder.beginControlFlow("for ($T tmp : $N.$N())", TypeName.get(mirror), this.convertArgumentName, String.format("get%s", NamingUtils.upperCamelCase(fieldName)));
        this.convertMethodBuilder.addStatement("$N.add($T.convertTo(tmp))", lowName, typeName);
        this.convertMethodBuilder.endControlFlow();
        this.convertMethodBuilder.addCode("\n}");
    }

    private void getterAndSetterMethodBuilder(VariableElement fieldElement, String fieldName, TypeName type) {
        String upperCamelCaseStr = NamingUtils.upperCamelCase(fieldName);
        String getterStr = String.format("get%s", upperCamelCaseStr);
        MethodSpec.Builder getter = MethodSpec.methodBuilder(getterStr).addModifiers(Modifier.PUBLIC).returns(type).addCode("\nreturn this.$N;\n", fieldName);

        String setterStr = String.format("set%s", upperCamelCaseStr);
        MethodSpec.Builder setter = MethodSpec.methodBuilder(setterStr).addModifiers(Modifier.PUBLIC).returns(TypeName.VOID).addCode("\n this.$N = $N;\n", fieldName, fieldName);

        ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, fieldName, Modifier.FINAL);

        visitor.visitor(fieldElement, getter);
        visitor.visitor(fieldElement, parameterBuilder);
        visitor.visitor(fieldElement, setter);
        setter.addParameter(parameterBuilder.build());

        masterBuilder.addMethod(setter.build());
        masterBuilder.addMethod(getter.build());
    }

    private TypeMirror getGenerics(Element element, int index) {
        TypeMirror type = element.asType();

        if (!(type instanceof DeclaredType)) {
            return null;
        }
        List<? extends TypeMirror> arguments = ((DeclaredType) type).getTypeArguments();
        if (arguments.isEmpty() || arguments.size() < index) {
            return null;
        }

        return arguments.get(index);
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

    }

    void buildFieldAndGetterSetter(VariableElement fieldElement) {
        String fieldName = fieldElement.getSimpleName().toString();
        TypeName type = TypeName.get(fieldElement.asType());
        FieldSpec.Builder fields = FieldSpec.builder(type, fieldName, Modifier.PUBLIC);

        ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, fieldName, Modifier.FINAL);

        visitor.visitor(fieldElement, fields);
        visitor.visitor(fieldElement, parameterBuilder);
        masterBuilder.addField(fields.build());
        this.getterAndSetterMethodBuilder(fieldElement, fieldName, type);
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

    public TypeName getType() {
        return this.returnedType;
    }

    public void toWrite(Filer filer) throws IOException {
        JavaFile
            .builder(this.returnedType.packageName(), this.masterBuilder.build()).
            build().writeTo(filer);
        for (DataTransferObjectBuilder builder : this.builders) {
            builder.toWrite(filer);
        }
    }
}
