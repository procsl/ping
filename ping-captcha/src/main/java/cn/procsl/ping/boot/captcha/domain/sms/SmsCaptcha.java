package cn.procsl.ping.boot.captcha.domain.sms;

import cn.procsl.ping.boot.captcha.domain.Captcha;
import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "c_captcha_sms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsCaptcha extends Captcha {

    @Id
    @GeneratedValue
    Long id;

    @Override
    protected boolean check(String ticket) throws VerifyFailureException {
        return false;
    }

    @Override
    public String message() {
        return null;
    }

}
