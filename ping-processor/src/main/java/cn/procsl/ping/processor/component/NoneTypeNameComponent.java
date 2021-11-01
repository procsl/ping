package cn.procsl.ping.processor.component;

import cn.procsl.ping.processor.ProcessorContext;

final class NoneTypeNameComponent implements TypeNameComponent {

    @Override
    public String getName() {
        return "None";
    }

    @Override
    public TypeNameComponent generateStruct(ProcessorContext context) {
        return null;
    }

}
