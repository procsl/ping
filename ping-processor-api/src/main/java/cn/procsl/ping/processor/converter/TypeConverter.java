package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.*;
import com.squareup.javapoet.*;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.stream.Collectors;

@SuperBuilder
class TypeConverter implements ModelConverter<TypeModel, TypeSpec> {

    @NonNull
    private final ModelConverter<FieldModel, FieldSpec> fieldModelToFieldSpecConverter;

    @NonNull
    private final ModelConverter<AnnotationModel, AnnotationSpec> annotationModelToAnnotationSpecConverter;

    @NonNull
    private final ModelConverter<MethodModel, MethodSpec> methodModelToMethodSpecConverter;

    @NonNull
    private final ModelConverter<NamingModel, TypeName> nameModelConverter;

    @NonNull
    private final ModelConverter<String, CodeBlock> codeBlockModelConverter;

    @Override
    public TypeSpec convertTo(TypeModel source) {


        TypeSpec.Builder typeSpec = this.getBuilder(source);

        Collection<Modifier> modifiers = source.getModifiers();
        if (modifiers != null) {
            typeSpec.addModifiers(modifiers.toArray(new Modifier[0]));
        }

        Collection<NamingModel> inters = source.getInterfaces();
        if (inters != null) {
            typeSpec.addSuperinterfaces(inters.stream().map(this.nameModelConverter::convertTo).collect(Collectors.toList()));
        }

        NamingModel clazz = source.getSuperClass();
        if (clazz != null) {
            typeSpec.superclass(this.nameModelConverter.convertTo(clazz));
        }

        Collection<AnnotationModel> annotation = source.getAnnotations();
        if (annotation != null) {
            typeSpec.addAnnotations(annotation.stream().map(this.annotationModelToAnnotationSpecConverter::convertTo).collect(Collectors.toList()));
        }

        Collection<FieldModel> fields = source.getFields();
        if (fields != null) {
            typeSpec.addFields(fields.stream().map(this.fieldModelToFieldSpecConverter::convertTo).collect(Collectors.toList()));
        }

        Collection<MethodModel> methods = source.getMethods();
        if (methods != null) {
            typeSpec.addMethods(methods.stream().map(this.methodModelToMethodSpecConverter::convertTo).collect(Collectors.toList()));
        }
        return typeSpec.build();
    }

    TypeSpec.Builder getBuilder(TypeModel source) {
        return TypeSpec.classBuilder(source.getType().getTypeName());
    }


}
