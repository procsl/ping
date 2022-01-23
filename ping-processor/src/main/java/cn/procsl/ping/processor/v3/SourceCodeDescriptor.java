package cn.procsl.ping.processor.v3;

import javax.lang.model.type.TypeMirror;

public interface SourceCodeDescriptor {

    /**
     * 是否返回void
     *
     * @return 如果返回void则为true
     */
    boolean isVoid();

    /**
     * @return 调用服务后的结果变量名称
     */
    String getResultVariableName();

    /**
     * @return 结果类型
     */
    TypeMirror getResultTypeMirror();

    /**
     * @return 获取方法调用源码
     */
    String getCallSourceCode();

    /**
     * @return 获取被调用的字段名称
     */
    String getCallFieldName();

    /**
     * @return 获取被调用的方法的名称
     */
    String getCallMethodName();
}
