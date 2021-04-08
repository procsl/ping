package cn.procsl.ping.processor.model;

import javax.lang.model.element.Modifier;
import java.util.Collection;

public interface MethodModel extends Model {

    String getMethodName();

    Collection<AnnotationModel> getAnnotations();

    Collection<Modifier> getModifiers();

    Collection<FieldModel> getArguments();

    Collection<NamingModel> getThrowable();

    NamingModel getReturn();

    CodeModel getBody();

    String getSignature();

}
