package cn.procsl.ping.processor.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface TypeDescriptor extends TypeNameDescriptor {

    <P, R> R accept(DescriptorVisitor<P, R> visitor);

    boolean isRootDescriptor();

    default Collection<String> getProps() {
        return Collections.emptyList();
    }

    List<FieldDescriptor> getFields();

    List<MethodDescriptor> getMethods();

}
