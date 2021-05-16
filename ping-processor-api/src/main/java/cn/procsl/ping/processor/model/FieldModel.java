package cn.procsl.ping.processor.model;

import lombok.Data;

import javax.lang.model.element.Modifier;
import java.util.Collection;

@Data
public class FieldModel {

    NamingModel type;

    String name;

    TypeModel parent;

    Collection<Modifier> modifiers;

    Collection<AnnotationModel> annotations;
}
