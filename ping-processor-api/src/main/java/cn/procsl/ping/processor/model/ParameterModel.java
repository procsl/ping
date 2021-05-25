package cn.procsl.ping.processor.model;

import lombok.Data;

import javax.lang.model.element.Modifier;
import java.util.Collection;

@Data
public class ParameterModel {

    NamingModel type;

    String name;

    MethodModel parent;

    Collection<Modifier> modifiers;

    Collection<AnnotationModel> annotations;
}
