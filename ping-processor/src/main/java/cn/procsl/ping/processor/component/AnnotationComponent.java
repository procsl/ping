package cn.procsl.ping.processor.component;

import cn.procsl.ping.processor.Component;
import com.squareup.javapoet.AnnotationSpec;
import com.sun.management.GarbageCollectionNotificationInfo;

import java.util.Collection;
import java.util.Collections;

public interface AnnotationComponent<E> extends Component<AnnotationSpec, E> {

    @Override
    default boolean addChild(Component<?, E> component) {
        return false;
    }

    @Override
    default boolean removeChild(Component<?, E> component) {
        return false;
    }

    @Override
    default Collection<Component<?, E>> getChildren() {
        return Collections.emptyList();
    }

    @Override
    default String getName() {
        return "annotation";
    }
}
