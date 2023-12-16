package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.web.annotation.Encryption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@Indexed
@RestController
public class CipherFilterController {


    @PostMapping(path = "/test/cipher/parameter")
    @Encryption(response = false)
    public Map<String, String> test1(@RequestParam Map<String, String> parameter) throws BusinessException {
        log.info("parameter: {}", parameter);
        return parameter;
    }

    @PostMapping(path = "/test/cipher/body")
    public Map<String, String> test2(@RequestBody Map<String, String> json) throws BusinessException {
        log.info("json: {}", json);
        return json;
    }

    @PostMapping(path = "/test/cipher/array")
    public String[] test3(@RequestParam String[] b) throws BusinessException {
        log.info("json: {}", b);
        return b;
    }


}
