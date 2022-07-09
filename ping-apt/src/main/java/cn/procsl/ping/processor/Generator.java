package cn.procsl.ping.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

public interface Generator {

    void init(ProcessingEnvironment processingEnv);

    boolean process(RoundEnvironment roundEnv);

}
