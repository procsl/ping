package cn.procsl.ping.processor.spring;

import cn.procsl.ping.processor.common.EmptyCodeModel;
import cn.procsl.ping.processor.common.GeneratedAnnotationModel;
import cn.procsl.ping.processor.common.TypeFieldModel;
import cn.procsl.ping.processor.model.*;
import lombok.NonNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SpringTypeModel implements TypeModel {

    /**
     * Service 接口或者类的编译器表示
     */
    @NonNull
    private final TypeElement typeElement;
    private final ArrayList<AnnotationModel> annotationModels = new ArrayList<>();
    private final ControllerMethodBuilder methodBuilder;
    private final TypeFieldModel field;

    public SpringTypeModel(@NonNull TypeElement typeElement) {
        this.typeElement = typeElement;
        this.annotationModels.add(new GeneratedAnnotationModel(this.getClass().getName()));
        this.methodBuilder = new ControllerMethodBuilder(typeElement);
        this.field = new TypeFieldModel(this.typeElement);
        field.addAnnotationModel(new AutowiredAnnotationModel());
    }

    @Override
    public NamingModel getName() {
        return new ControllerNamingModel(typeElement);
    }

    @Override
    public Type getType() {
        return Type.CLASS;
    }

    @Override
    public Collection<Modifier> getModifiers() {
        return Collections.singletonList(Modifier.PUBLIC);
    }

    @Override
    public Collection<NamingModel> getImported() {
        return Collections.emptyList();
    }

    @Override
    public Collection<NamingModel> getStaticImported() {
        return Collections.emptyList();
    }

    @Override
    public CodeModel getInitCode() {
        return EmptyCodeModel.INSTANCE;
    }

    @Override
    public CodeModel getStaticInitCode() {
        return EmptyCodeModel.INSTANCE;
    }

    @Override
    public Collection<FieldModel> getFields() {
        return Collections.singletonList(field);
    }

    @Override
    public Collection<MethodModel> getMethods() {
        return methodBuilder.getMethods();
    }

    @Override
    public Collection<AnnotationModel> getAnnotations() {
        return this.annotationModels;
    }

    @Override
    public Collection<VariableNamingModel> getInterfaces() {
        return null;
    }

    @Override
    public VariableNamingModel getSuperClass() {
        return null;
    }

    @Override
    public Collection<VariableNamingModel> getTypeVariables() {
        return null;
    }
}
