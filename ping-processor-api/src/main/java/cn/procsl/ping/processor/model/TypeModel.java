package cn.procsl.ping.processor.model;

import lombok.Data;

import javax.lang.model.element.Modifier;
import java.util.Collection;

@Data
public class TypeModel {

    NamingModel type;

    Collection<Modifier> modifiers;

    Collection<NamingModel> interfaces;

    NamingModel superClass;

    Collection<FieldModel> fields;

    Collection<AnnotationModel> annotations;

    Collection<MethodModel> methods;

}
