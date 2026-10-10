package cn.procsl.ping.boot.ui.component;

import cn.procsl.ping.boot.ui.component.api.ResourceReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 引用合法性测试夹具：带 {@link ResourceReference} 标注的处理方法。
 */
@RestController
@RequestMapping("/v1/ui-fixture")
public class AnnotatedFixture {

    @GetMapping("/annotated")
    @ResourceReference(name = "获取列表")
    public Map<String, String> annotated() {
        return Map.of("ok", "true");
    }
}
