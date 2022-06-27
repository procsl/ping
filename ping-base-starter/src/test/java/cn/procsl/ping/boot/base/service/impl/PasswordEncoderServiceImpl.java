package cn.procsl.ping.boot.base.service.impl;

import cn.procsl.ping.boot.domain.service.PasswordEncoderService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

@Indexed
@Component
public class PasswordEncoderServiceImpl implements PasswordEncoderService {
    @Override
    public String encode(CharSequence password) {
        return String.valueOf(password);
    }
}
