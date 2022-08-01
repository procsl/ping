package cn.procsl.ping.boot.captcha.web;

import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.common.dto.MessageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
@RequestMapping
@Tag(name = "captcha", description = "图形验证码")
public class ImageCaptchaControllerTest {

    @ResponseBody
    @GetMapping("/v1/test")
    @VerifyCaptcha(type = CaptchaType.image)
    @Operation(summary = "测试接口")
    public String test() {
        return "hello world";
    }

    @ResponseBody
    @GetMapping("/v1/encoder")
    @Operation(summary = "编码接口")
    public MessageDTO decoder(@RequestParam("code") String code) {
        String msg = Base64.getEncoder().encodeToString(code.getBytes(StandardCharsets.UTF_8));
        return new MessageDTO(msg);
    }

}