package cn.procsl.ping.boot.captcha.web;

import com.wf.captcha.base.Captcha;
import lombok.RequiredArgsConstructor;

import java.io.OutputStream;

@RequiredArgsConstructor
public class EnhanceImageCaptchaWrapper extends Captcha {

    final Captcha captcha;

    @Override
    public boolean out(OutputStream os) {
        return captcha.out(os);
    }

    @Override
    public String toBase64() {
        return captcha.toBase64();
    }

}
