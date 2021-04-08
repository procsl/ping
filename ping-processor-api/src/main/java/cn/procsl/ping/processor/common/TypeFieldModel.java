package cn.procsl.ping.processor.common;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.FieldModel;
import cn.procsl.ping.processor.model.NamingModel;
import cn.procsl.ping.processor.utils.NamingUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class TypeFieldModel implements FieldModel {

    @NonNull
    final TypeElement typeElement;

    private final ArrayList<AnnotationModel> annotationModels = new ArrayList<>();

    @Override
    public NamingModel getType() {
        return new TypeNamingModel(typeElement);
    }

    @Override
    public String getFieldName() {
        String name = typeElement.getSimpleName().toString();
        return NamingUtils.lowerCamelCase(name);
    }

    @Override
    public Collection<Modifier> getModifiers() {
        return Collections.singletonList(Modifier.PRIVATE);
    }

    @Override
    public Collection<AnnotationModel> getAnnotations() {
        return Collections.unmodifiableList(annotationModels);
    }

    public void addAnnotationModel(@NonNull AnnotationModel annotationModel) {
        this.annotationModels.add(annotationModel);
    }
}
