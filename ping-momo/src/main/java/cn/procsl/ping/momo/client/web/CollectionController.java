package cn.procsl.ping.momo.client.web;


import cn.procsl.ping.momo.client.service.PageTransformService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Indexed
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "momo", description = "momo插件客户端")
@Slf4j
public class CollectionController {

    private final PageTransformService pageTransformService;

    @PostMapping("/v1/momo/collector")
    @ResponseBody
    public void collector(@RequestBody Map<String, Object> data) {
        log.info("收到消息:{}", data);
    }

    @PostMapping("/v1/momo/page")
    @ResponseBody
    public void savePage(@RequestBody Map<String, Object> data) {
    }

}
