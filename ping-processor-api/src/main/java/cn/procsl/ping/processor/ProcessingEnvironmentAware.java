package cn.procsl.ping.processor;

import javax.annotation.Nonnull;
import javax.annotation.processing.ProcessingEnvironment;

public interface ProcessingEnvironmentAware {
    void setProcessingEnvironment(@Nonnull ProcessingEnvironment processingEnv);
}
