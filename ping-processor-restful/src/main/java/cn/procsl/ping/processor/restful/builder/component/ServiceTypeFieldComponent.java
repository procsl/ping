package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.component.AnnotationComponent;
import cn.procsl.ping.processor.component.FieldComponent;
import cn.procsl.ping.processor.component.TypeNameComponent;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collection;

public class ServiceTypeFieldComponent extends FieldComponent {

    final TypeElement typeElement;

    public ServiceTypeFieldComponent(TypeElement serviceElement) {
        this.typeElement = serviceElement;
    }

    @Override
    public Collection<AnnotationComponent> getFieldAnnotations() {
        return new ArrayList<>();
    }

    @Override
    public TypeNameComponent getType() {
        return super.getType();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public Modifier getModifier() {
        return super.getModifier();
    }
}
