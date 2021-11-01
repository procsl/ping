package cn.procsl.ping.processor.component;

import cn.procsl.ping.processor.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractComponent<T, E> implements Component<T, E> {

    protected final ArrayList<Component<?, E>> children = new ArrayList<>();

    @Override
    public boolean addChild(Component<?, E> component) {
        return children.add(component);
    }

    @Override
    public boolean removeChild(Component<?, E> component) {
        return children.remove(component);
    }

    @Override
    public Collection<Component<?, E>> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    @Override
    public String getName() {
        return "";
    }
}
