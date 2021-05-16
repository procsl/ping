package cn.procsl.ping.processor.model;

import lombok.Data;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.List;

@Data
public class MethodModel {

    TypeModel parent;

    String name;

    Collection<AnnotationModel> annotations;

    List<ParameterModel> parameters;

    Collection<NamingModel> throwable;

    Collection<Modifier> modifiers;

    String body;

    NamingModel returned;
}
