package cn.procsl.ping.boot.captcha.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
@DiscriminatorValue("email")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailCaptcha extends Captcha {
    @Override
    protected void check(String ticket) throws VerifyFailureException {

    }

}
