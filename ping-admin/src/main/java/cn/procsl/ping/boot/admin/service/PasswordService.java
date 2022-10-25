package cn.procsl.ping.boot.admin.service;

import cn.procsl.ping.boot.common.service.PasswordEncoderService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;


@Indexed
@Component
public class PasswordService implements PasswordEncoderService {

    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
    }

}
