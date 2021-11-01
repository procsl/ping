package cn.procsl.ping.processor.component;

import cn.procsl.ping.processor.Component;
import cn.procsl.ping.processor.ProcessorContext;
import com.squareup.javapoet.TypeName;

public class ClassTypeNameComponent<E> implements TypeNameComponent<E> {

    @Override
    public boolean removeChild(Component<?, E> component) {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public TypeName generateStruct(ProcessorContext context, E element) {
        return null;
    }
}
