package cn.procsl.ping.boot.captcha.domain;

import java.io.IOException;
import java.io.OutputStream;

public interface CaptchaGenerator {

    void generated(String id, CaptchaType type, OutputStream os) throws IOException;

}
