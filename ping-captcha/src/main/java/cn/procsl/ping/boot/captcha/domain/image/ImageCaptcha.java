package cn.procsl.ping.boot.captcha.domain.image;

import cn.procsl.ping.boot.captcha.domain.Captcha;
import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;
import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Base64;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "c_captcha_image")
@JsonIgnoreProperties("new")
@RepositoryCreator(builders = "org.springframework.data.jpa.repository.JpaSpecificationExecutor",
        repositoryName = "ImageCaptchaSpecificationExecutor")
public class ImageCaptcha extends Captcha {

    public final static String token_key = "image-captcha-token";

    @Id
    Long id;

    @Builder
    public ImageCaptcha(Long id, @NonNull String target, @NonNull String ticket, int expired) {
        super(target, ticket, expired);
        this.id = id;
    }

    @Override
    protected boolean check(String ticket) throws VerifyFailureException {
        String str = this.parse(ticket);
        return this.ticket.equalsIgnoreCase(str);
    }

    @Override
    public String message() {
        return CaptchaType.image.message;
    }

    protected String parse(String ticket) throws VerifyFailureException {
        try {
            byte[] str = Base64.getDecoder().decode(ticket);
            return new String(str);
        } catch (IllegalArgumentException e) {
            throw new VerifyFailureException(e, false, "%s验证码错误", this.message());
        }

    }


    /**
     * 有效秒数
     *
     * @return second time
     */
    public int validSecond() {
        long second = (this.expiredDate.getTime() - this.createDate.getTime()) / 1000;
        if (second <= Integer.MAX_VALUE) {
            return (int) second;
        }
        throw new IllegalStateException("错误的超时时间");
    }
}
