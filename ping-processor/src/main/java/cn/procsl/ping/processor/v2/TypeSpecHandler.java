package cn.procsl.ping.processor.v2;

import cn.procsl.ping.processor.ProcessorEnvironment;
import com.squareup.javapoet.TypeSpec;

/**
 * 处理器， 用来处理不同节点
 */
public interface TypeSpecHandler {

    /**
     * 生成不同代码片段的处理器
     *
     * @param builder     处理器节点元素
     * @param environment 环境上下文及工具
     */
    void handle(TypeSpec.Builder builder, ProcessorEnvironment environment);

    /**
     * 不同处理器的顺序
     *
     * @return 排序方式为从大到小的方式， 越大优先级越高
     */
    default int order() {
        return 0;
    }

}
