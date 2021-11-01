package cn.procsl.ping.processor.component;

import cn.procsl.ping.processor.Component;
import cn.procsl.ping.processor.ProcessorContext;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class GeneralTypeNameComponent implements TypeNameComponent<String> {

    final String packageName;

    final String className;


    @Override
    public boolean removeChild(Component<?, String> component) {
        throw new UnsupportedOperationException("不支持的操作");
    }

    @Override
    public String getName() {
        return String.format("%s.%s", packageName, className);
    }

    @Override
    public TypeName generateStruct(ProcessorContext context, String element) {
        return ClassName.get(packageName, className);
    }


}
