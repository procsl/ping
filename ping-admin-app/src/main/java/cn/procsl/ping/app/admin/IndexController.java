package cn.procsl.ping.app.admin;

import cn.procsl.ping.boot.rest.annotation.RestEndpoint;
import io.swagger.v3.oas.annotations.Operation;
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
    @RouterOperation(operation = @Operation(operationId = "index"))
    public Map<String, String> index() {
        return Map.of("root", "123456");
    }


}
