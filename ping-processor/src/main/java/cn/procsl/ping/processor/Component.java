package cn.procsl.ping.processor;

import java.util.Collection;

public interface Component<T, E> {

    boolean addChild(Component<?, E> component);

    boolean removeChild(Component<?, E> component);

    Collection<Component<?, E>> getChildren();

    String getName();

    T generateStruct(ProcessorContext context, E element);

}
