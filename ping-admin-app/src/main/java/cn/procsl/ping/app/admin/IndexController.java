package cn.procsl.ping.app.admin;

import cn.procsl.ping.boot.rest.annotation.RestEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Map;

@RestEndpoint
@RequestMapping
@Tag(name = "这是Tag", description = "这是tag.description")
@Validated
public class IndexController {

    @PostMapping(path = {"index/{id}"})

    @Operation(summary = "这是这个接口的摘要", description = "这是这个接口的描述", operationId = "index")
    @ApiResponse(responseCode = "200", description = "这是ApiResponse描述")
    @ApiResponse(responseCode = "201", description = "这是ApiResponse描述1")
    @Valid
    public Map<String, String> index(@Size(min = 1, max = 100, message = "区间") @Deprecated(forRemoval = true)
                                     @Parameter(description = "这是参数描述")
                                     @PathVariable("id") String id) {
        return Map.of("root", "123456");
    }


}
