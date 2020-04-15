package cn.procsl.ping.boot.doc.core;

import org.springdoc.api.AbstractOpenApiResource;
import org.springdoc.core.*;
import org.springdoc.core.customizers.OpenApiCustomiser;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author procsl
 * @date 2020/02/25
 */
public class RestOpenApiResource extends AbstractOpenApiResource {

    public RestOpenApiResource(String groupName,
                               OpenAPIBuilder openAPIBuilder,
                               AbstractRequestBuilder requestBuilder,
                               GenericResponseBuilder responseBuilder,
                               OperationBuilder operationParser,
                               Optional<List<OpenApiCustomiser>> openApiCustomisers,
                               SpringDocConfigProperties springDocConfigProperties) {
        super(groupName, openAPIBuilder, requestBuilder, responseBuilder, operationParser, openApiCustomisers, springDocConfigProperties);
    }

    @Override
    protected void getPaths(Map<String, Object> findRestControllers) {
    }
}
