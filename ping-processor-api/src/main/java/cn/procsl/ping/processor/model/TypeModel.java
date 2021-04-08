package cn.procsl.ping.processor.model;

import javax.lang.model.element.Modifier;
import java.util.Collection;

public interface TypeModel extends Model {

    NamingModel getName();

    Type getType();

    Collection<Modifier> getModifiers();

    Collection<NamingModel> getImported();

    Collection<NamingModel> getStaticImported();

    CodeModel getInitCode();

    CodeModel getStaticInitCode();

    Collection<FieldModel> getFields();

    Collection<MethodModel> getMethods();

    Collection<AnnotationModel> getAnnotations();

    Collection<VariableNamingModel> getInterfaces();

    VariableNamingModel getSuperClass();

    Collection<VariableNamingModel> getTypeVariables();


    enum Type {
        CLASS, INTERFACE, ENUM, ANONYMOUS_CLASS

    }
}
