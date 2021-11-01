package cn.procsl.ping.processor.component;

import cn.procsl.ping.processor.Component;
import cn.procsl.ping.processor.ProcessorContext;
import com.squareup.javapoet.CodeBlock;

import java.util.Collection;
import java.util.Collections;

public final class EmptyCodeBlockComponent implements CodeBlockComponent {

    @Override
    public boolean addChild(Component<?> component) {
        return false;
    }

    @Override
    public Collection<Component<?>> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "empty-code";
    }

    @Override
    public CodeBlock generateStruct(ProcessorContext context) {
        return null;
    }
}
