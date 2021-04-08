package cn.procsl.ping.processor.model;

import javax.lang.model.element.Modifier;
import java.util.Collection;

public interface FieldModel extends Model {

    NamingModel getType();

    String getFieldName();

    Collection<Modifier> getModifiers();

    Collection<AnnotationModel> getAnnotations();
}
