package cn.procsl.ping.processor.model;

import lombok.Data;

import java.lang.reflect.Modifier;
import java.util.Collection;

@Data
public class ParameterModel {

    NamingModel type;

    String name;

    MethodModel parent;

    Collection<Modifier> modifiers;

    Collection<AnnotationModel> annotations;
}
