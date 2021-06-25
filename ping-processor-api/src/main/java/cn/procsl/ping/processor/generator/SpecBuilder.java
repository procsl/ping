package cn.procsl.ping.processor.generator;

import cn.procsl.ping.processor.ProcessorContext;

public interface SpecBuilder<S, T> {

    T build(ProcessorContext context, S source);

}
