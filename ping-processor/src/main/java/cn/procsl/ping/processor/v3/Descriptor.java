package cn.procsl.ping.processor.v3;

public interface Descriptor {

    <P, R> R accept(TargetVisitor<P, R> visitor);

    default TypeDescriptor getTypeDescriptor() {
        return null;
    }

}
