package cn.procsl.ping.boot.captcha.domain.sms;

import cn.procsl.ping.boot.captcha.domain.Captcha;
import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;
import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "c_captcha_sms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RepositoryCreator
public class SmsCaptcha extends Captcha {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
    Long id;

    @Override
    public String message() {
        return null;
    }

}
