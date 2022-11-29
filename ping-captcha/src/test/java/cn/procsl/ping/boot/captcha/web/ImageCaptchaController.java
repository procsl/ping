package cn.procsl.ping.boot.captcha.web;

import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.common.dto.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Controller
@RequestMapping
@Tag(name = "captcha", description = "图形验证码")
public class ImageCaptchaController {

    @ResponseBody
    @GetMapping("/v1/test")
    @VerifyCaptcha(type = CaptchaType.image)
    @Operation(summary = "测试接口")
    public String test() {
        return "{}";
    }

    @ResponseBody
    @GetMapping("/v1/encoder")
    @Operation(summary = "编码接口")
    public MessageVO decoder(@RequestParam("code") String code) {
        String msg = Base64.getEncoder().encodeToString(code.getBytes(StandardCharsets.UTF_8));
        return new MessageVO(msg);
    }

    @GetMapping("/*")
    public void index(HttpServletRequest request) {
        log.info("info:{}", request.getQueryString());
    }


}