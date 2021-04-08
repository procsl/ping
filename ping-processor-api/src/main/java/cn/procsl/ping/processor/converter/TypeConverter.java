package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.*;
import com.squareup.javapoet.*;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.stream.Collectors;

@SuperBuilder
public class TypeConverter extends AbstractAwareConvertor<TypeModel, TypeSpec> {

    @NonNull
    private final ModelConverter<FieldModel, FieldSpec> fieldModelToFieldSpecConverter;

    @NonNull
    private final ModelConverter<AnnotationModel, AnnotationSpec> annotationModelToAnnotationSpecConverter;

    @NonNull
    private final ModelConverter<MethodModel, MethodSpec> methodModelToMethodSpecConverter;

    @NonNull
    private final ModelConverter<VariableNamingModel, TypeVariableName> variableNamingModelTypeNameConverter;

    @NonNull
    private final ModelConverter<CodeModel, CodeBlock> codeBlockModelConverter;

    @Override
    protected TypeSpec convertTo(TypeModel source) {


        TypeSpec.Builder typeSpec = this.getBuilder(source);

        Collection<Modifier> modifiers = source.getModifiers();
        if (modifiers != null) {
            typeSpec.addModifiers(modifiers.toArray(new Modifier[0]));
        }

        Collection<VariableNamingModel> inters = source.getInterfaces();
        if (inters != null) {
            typeSpec.addSuperinterfaces(inters.stream().map(this.variableNamingModelTypeNameConverter::to).collect(Collectors.toList()));
        }

        VariableNamingModel clazz = source.getSuperClass();
        if (clazz != null) {
            typeSpec.superclass(this.variableNamingModelTypeNameConverter.to(clazz));
        }

        Collection<VariableNamingModel> variable = source.getTypeVariables();
        if (variable != null) {
            typeSpec.addTypeVariables(variable.stream().map(this.variableNamingModelTypeNameConverter::to).collect(Collectors.toList()));
        }


        CodeModel staticCode = source.getInitCode();
        if (staticCode != null) {
            typeSpec.addStaticBlock(this.codeBlockModelConverter.to(staticCode));
        }

        CodeModel initCode = source.getInitCode();
        if (initCode != null) {
            typeSpec.addInitializerBlock(this.codeBlockModelConverter.to(initCode));
        }

        Collection<AnnotationModel> annotation = source.getAnnotations();
        if (annotation != null) {
            typeSpec.addAnnotations(annotation.stream().map(this.annotationModelToAnnotationSpecConverter::to).collect(Collectors.toList()));
        }

        Collection<FieldModel> fields = source.getFields();
        if (fields != null) {
            typeSpec.addFields(fields.stream().map(this.fieldModelToFieldSpecConverter::to).collect(Collectors.toList()));
        }

        Collection<MethodModel> methods = source.getMethods();
        if (methods != null) {
            typeSpec.addMethods(methods.stream().map(this.methodModelToMethodSpecConverter::to).collect(Collectors.toList()));
        }
        return typeSpec.build();
    }

    TypeSpec.Builder getBuilder(TypeModel source) {
        NamingModel name = source.getName();
        switch (source.getType()) {
            case CLASS:
                return TypeSpec.classBuilder(name.getName());
            case ENUM:
                return TypeSpec.enumBuilder(name.getName());
            case INTERFACE:
                return TypeSpec.interfaceBuilder(name.getName());
            case ANONYMOUS_CLASS:
                return TypeSpec.anonymousClassBuilder(name.getName());
        }
        throw new UnsupportedOperationException("不支持的转换");
    }


}
