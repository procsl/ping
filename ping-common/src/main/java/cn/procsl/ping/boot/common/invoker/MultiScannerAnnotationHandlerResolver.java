package cn.procsl.ping.boot.common.invoker;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public final class MultiScannerAnnotationHandlerResolver implements
        HandlerResolver<AnnotationHandlerInvokerContext> {

    final Collection<ScannerAnnotationHandlerResolver> resolvers;

    public MultiScannerAnnotationHandlerResolver(Collection<Class<? extends Annotation>> classes) {
        resolvers = classes.stream().map(ScannerAnnotationHandlerResolver::new)
                           .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<AnnotationHandlerInvokerContext> resolve(Object target) {
        ArrayList<AnnotationHandlerInvokerContext> result = new ArrayList<>();
        resolvers.stream()
                 .map(item -> item.resolve(target))
                 .forEach(result::addAll);
        return result;
    }

}
