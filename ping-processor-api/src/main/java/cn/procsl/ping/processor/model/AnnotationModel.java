package cn.procsl.ping.processor.model;

import java.util.Collection;

public interface AnnotationModel extends Model {

    NamingModel getName();

    CodeModel getCode(String fieldName);

    Collection<String> getFieldNames();

}
