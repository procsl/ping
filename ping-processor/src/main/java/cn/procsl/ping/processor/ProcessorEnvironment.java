package cn.procsl.ping.processor;

import javax.annotation.Nonnull;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

public interface ProcessorEnvironment {


    ProcessingEnvironment getProcessingEnvironment();

    /**
     * @return 获取编译时日志组件
     */
    Messager getMessager();

    /**
     * @return 获取编译时文件
     */
    Filer getFiler();


    /**
     * @param key 系统配置key
     * @return 返回找到的系统配置
     */
    String getConfig(@Nonnull String key);

}
