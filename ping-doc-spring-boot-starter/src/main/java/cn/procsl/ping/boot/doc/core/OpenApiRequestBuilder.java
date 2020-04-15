package cn.procsl.ping.boot.doc.core;

import org.springdoc.core.GenericParameterBuilder;
import org.springdoc.core.OperationBuilder;
import org.springdoc.core.RequestBodyBuilder;
import org.springdoc.core.RequestBuilder;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;

import java.util.List;
import java.util.Optional;

/**
 * @author procsl
 * @date 2020/02/25
 */
public class OpenApiRequestBuilder extends RequestBuilder {

    public OpenApiRequestBuilder(GenericParameterBuilder parameterBuilder, RequestBodyBuilder requestBodyBuilder, OperationBuilder operationBuilder, Optional<List<OperationCustomizer>> customizers, Optional<List<ParameterCustomizer>> parameterCustomizers) {
        super(parameterBuilder, requestBodyBuilder, operationBuilder, customizers, parameterCustomizers);
    }


}
