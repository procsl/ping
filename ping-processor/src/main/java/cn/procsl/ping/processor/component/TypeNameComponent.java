package cn.procsl.ping.processor.component;

import cn.procsl.ping.processor.Component;
import com.squareup.javapoet.TypeName;

import java.util.Collection;
import java.util.Collections;

public interface TypeNameComponent<E> extends Component<TypeName, E> {

    @Override
    default boolean addChild(Component<?, E> component) {
        throw new UnsupportedOperationException("不支持的操作");
    }

    @Override
    default Collection<Component<?, E>> getChildren() {
        return Collections.emptyList();
    }
}
