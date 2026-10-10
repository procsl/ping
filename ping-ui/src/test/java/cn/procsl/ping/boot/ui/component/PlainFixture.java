package cn.procsl.ping.boot.ui.component;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 引用合法性测试夹具：故意<b>不加</b> {@code @ResourceReference}，
 * 用于证明未标注的接口不可被装配描述引用（即使 OpenAPI 中存在同名 summary）。
 */
@RestController
@RequestMapping("/v1/ui-fixture")
public class PlainFixture {

    @GetMapping("/plain")
    public Map<String, String> plain() {
        return Map.of("ok", "true");
    }
}
