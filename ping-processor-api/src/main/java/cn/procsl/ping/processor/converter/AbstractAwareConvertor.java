package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.ProcessingEnvironmentAware;
import cn.procsl.ping.processor.RoundEnvironmentAware;
import cn.procsl.ping.processor.model.Model;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.annotation.Nonnull;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

@RequiredArgsConstructor
@SuperBuilder
public abstract class AbstractAwareConvertor<S extends Model, T> implements ModelConverter<S, T> {

    @Nonnull
    final private ProcessingEnvironment processingEnv;

    @Nonnull
    final private RoundEnvironment roundEnv;

    @Override
    public final T to(@NonNull S source) {
        if (source instanceof ProcessingEnvironmentAware) {
            ((ProcessingEnvironmentAware) source).setProcessingEnvironment(this.processingEnv);
        }

        if (source instanceof RoundEnvironmentAware) {
            ((RoundEnvironmentAware) source).setRoundEnvironment(roundEnv);
        }
        return this.convertTo(source);
    }

    protected abstract T convertTo(S source);

}
