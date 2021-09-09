package cn.procsl.ping.processor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.function.Function;

@RequiredArgsConstructor
public class SimpleProcessorContext implements ProcessorContext {

    @Getter
    final private RoundEnvironment roundEnvironment;

    @Getter
    final private ProcessingEnvironment processingEnvironment;

    @Getter
    final private Messager messager;

    @Getter
    final private Filer filer;

    final private Function<String, String> config;

    @Override
    public String getConfig(String key) {
        return config.apply(t);
    }
}
