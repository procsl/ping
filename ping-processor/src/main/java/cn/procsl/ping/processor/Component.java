package cn.procsl.ping.processor;

public interface Component<T, E> {

    default String getName() {
        return this.getClass().getName();
    }

    T generateStruct(ProcessorContext context, E element);

}
