package cn.procsl.ping.momo.client.web;


//import io.swagger.v3.oas.annotations.tags.Tag;

import cn.procsl.ping.momo.client.service.ProcessorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//@Indexed
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "momo", description = "momo插件客户端")
@Slf4j
public class CollectionController {

    private final ProcessorService processorService;

    @PostMapping("/v1/momo/collector")
    @ResponseBody
    public ResultActionVO collector(@RequestBody Map<String, Object> data) {
        log.info("收到消息:{}", data);
        return new ResultActionVO(processorService.processor(data));
    }


}
