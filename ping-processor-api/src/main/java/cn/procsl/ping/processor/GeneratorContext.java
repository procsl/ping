package cn.procsl.ping.processor;

import lombok.NonNull;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;


public interface GeneratorContext {

    RoundEnvironment getRoundEnvironment();

    ProcessingEnvironment getProcessingEnvironment();

    Messager getMessager();

    Filer getFiler();

    String getConfig(String key);

    Set<? extends TypeElement> getAnnotations();

    void setAttribute(@NonNull String key, Object attr);

    Object getAttribute(@NonNull String key);

}
