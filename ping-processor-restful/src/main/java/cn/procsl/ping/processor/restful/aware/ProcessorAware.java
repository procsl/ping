package cn.procsl.ping.processor.restful.aware;

import javax.annotation.processing.ProcessingEnvironment;

public interface ProcessorAware {

    void setProcessingEnv(ProcessingEnvironment processingEnv);

}
