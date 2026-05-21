package cn.procsl.ping.boot.captcha.domain.image;

import cn.procsl.ping.boot.captcha.domain.Captcha;
import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptchaCommand;
import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;
import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Base64;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "c_captcha_image")
@JsonIgnoreProperties("new")
@RepositoryCreator(repositories = JpaSpecificationExecutor.class,
    repositoryName = "ImageCaptchaSpecificationExecutor")
public class ImageCaptcha extends Captcha {

    public final static String TOKEN_KEY = "image-captcha-token";

    @Id
    Long id;


    @Builder
    public ImageCaptcha(Long id, @NonNull String target, @NonNull String ticket, @NonNull String functionId, int expired) {
        super(target, ticket, functionId, expired);
        this.id = id;
    }


    @Override
    public boolean check(@NonNull VerifyCaptchaCommand param) throws VerifyFailureException {
//        String str = this.parseBase64Ticket(param.getClientTicket());
        return this.ticket.equalsIgnoreCase(param.getClientTicket());
    }

    @Override
    public String message() {
        return CaptchaType.image.message;
    }

    protected String parseBase64Ticket(String ticket) throws VerifyFailureException {
        try {
            byte[] str = Base64.getDecoder().decode(ticket);
            return new String(str);
        } catch (IllegalArgumentException e) {
            throw new VerifyFailureException(e, false, "%s验证码错误", this.message());
        }

    }

    public String parseMethod() {
        return functionId.split(":")[0];
    }

    public String parsePath() {
        return functionId.split(":")[1];
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
