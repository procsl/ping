package cn.procsl.ping.processor;

import javax.annotation.Nonnull;
import javax.annotation.processing.RoundEnvironment;

public interface RoundEnvironmentAware {
    void setRoundEnvironment(@Nonnull RoundEnvironment roundEnv);
}
