package cn.procsl.ping.boot.captcha.domain.email;

import cn.procsl.ping.boot.captcha.domain.Captcha;
import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@RepositoryCreator
@Table(name = "c_captcha_email")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailCaptcha extends Captcha {

    @Id
    @GeneratedValue
    Long id;

    @Column(updatable = false, length = 50)
    String email;

    public EmailCaptcha(String target, String email) {
        super(target, random(6, number_chars), 6);
        this.email = email;
    }

    public int validMinute() {
        long nano = this.expiredDate.getTime() - this.createDate.getTime();
        return (int) nano / 1000 / 60;
    }

    @Override
    public String message() {
        return CaptchaType.email.message;
    }
}
