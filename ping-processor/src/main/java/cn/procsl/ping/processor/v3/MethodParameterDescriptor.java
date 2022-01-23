package cn.procsl.ping.processor.v3;

public interface MethodParameterDescriptor extends TypeNameDescriptor {

    /**
     * 是否是原始类型
     *
     * @return 如果是新类型
     */
    boolean isNew();

    int getIndex();

    String getParameterName();


}
