package cn.procsl.ping.processor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;


public interface ProcessorContext {

    RoundEnvironment getRoundEnvironment();

    ProcessingEnvironment getProcessingEnvironment();

    Messager getMessager();

    Filer getFiler();

    String getConfig(String key);

//    AnnotationVisitor getVisitor(String supportType);
//
//    String createFieldName(Element element);

    default String getRootPackageName() {
        return this.getConfig("creator.api.packageName");
    }
}
