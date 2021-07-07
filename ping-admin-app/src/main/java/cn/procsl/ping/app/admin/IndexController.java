package cn.procsl.ping.app.admin;

import cn.procsl.ping.boot.rest.annotation.RestEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RestEndpoint
@RequestMapping
@Tag(name = "这是Tag", description = "这是tag.description")
public class IndexController {

    @GetMapping(path = {"index"})
//    @RouterOperation(operation = @Operation(operationId = "index", summary = "这是摘要", description = "这是描述"))
    @ApiResponse(responseCode = "200", description = "这是ApiResponse描述", content = @Content())
    @Parameter(name = "id", description = "这是参数描述", in = ParameterIn.QUERY)
    public Map<String, String> index(String id) {
        return Map.of("root", "123456");
    }


}
