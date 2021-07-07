package cn.procsl.ping.processor.api;

import lombok.NonNull;

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

    void setAttribute(@NonNull String key, Object attr);

    Object getAttribute(@NonNull String key);

}
