package cn.procsl.ping.boot.captcha.domain;

import lombok.*;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Base64;
import java.util.Date;

@Getter
@Setter
@Entity
@DiscriminatorValue("image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageCaptcha extends Captcha {

    @Builder
    public ImageCaptcha(@NonNull String target, @NonNull String ticket, int expired) {
        this.target = target;
        this.ticket = ticket;
        this.expired = this.expired(expired);
        this.createDate = new Date();
    }

    @Override
    protected void check(String ticket) throws VerifyFailureException {
        String str = this.parse(ticket);
        if (!this.ticket.equalsIgnoreCase(str)) {
            throw new VerifyFailureException("%s验证码错误", this.message());
        }
    }

    protected String parse(String ticket) throws VerifyFailureException {
        try {
            byte[] str = Base64.getDecoder().decode(ticket);
            return new String(str);
        } catch (IllegalArgumentException e) {
            throw new VerifyFailureException(e, "%s验证码错误", this.message());
        }

    }

}
