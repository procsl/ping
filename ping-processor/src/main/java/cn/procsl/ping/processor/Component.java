package cn.procsl.ping.processor;

public interface Component<T, E> {

    String getName();

    T generateStruct(ProcessorContext context, E element);

}
