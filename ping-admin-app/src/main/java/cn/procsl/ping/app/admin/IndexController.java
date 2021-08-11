package cn.procsl.ping.app.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
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
                                     @PathVariable("id") String id,
                                     @RequestBody List<@Size(min = 10, max = 20) String> list
    ) {
        return Map.of("root", id);
    }

    @PutMapping(path = "map-test")
    public Map<String, String> mapTest(@RequestBody Map<@Size(min = 3, max = 10) String, @Email @NotBlank String> map) {
        return map;
    }

    @PutMapping(path = "response")
    public ResponseEntity<String> putResponse(String code) {
        return ResponseEntity.created(URI.create("https://procsl.cn/hello.json")).body(code);
    }

    @GetMapping(path = "response")
    public ResponseEntity<String> getResponse(@RequestParam(defaultValue = "123") @Parameter String code) {
        return ResponseEntity.created(URI.create("https://procsl.cn/hello.json")).body(code);
    }

}
