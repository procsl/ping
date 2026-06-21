package cn.procsl.ping.app.query;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Indexed
@RestController
@RequestMapping
public class TestController {

    @Getter
    @Setter
    public static class Entity {
        String nameName;
        String nameValue;
    }

    @PostMapping("/v1/echo")
    public Entity echo(@RequestBody Entity entity) {
        return entity;
    }

}
