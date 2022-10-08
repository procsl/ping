package cn.procsl.ping.boot.captcha.domain;


import cn.procsl.ping.boot.common.jpa.DiscriminatorValueFinder;
import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
@Entity
@RepositoryCreator
@Table(name = "c_captcha")
@DiscriminatorColumn(name = "type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Captcha extends AbstractPersistable<Long> implements Serializable, DiscriminatorValueFinder {

    @Column(length = 36, updatable = false)
    String target;

    @Column(length = 10, updatable = false)
    String ticket;

    @Column(updatable = false)
    Date expired;

    @Column(updatable = false)
    Date createDate;

    public void verify(@NonNull String ticket) throws VerifyFailureException {
        // 如果当前时间是过期时间之后, 则属于验证码超时
        if (new Date().after(expired)) {
            throw new VerifyFailureException("%s验证码已过期", this.message());
        }

        this.check(ticket);
    }

    protected abstract void check(String ticket) throws VerifyFailureException;

    public String message() {
        return CaptchaType.valueOf(this.find()).message;
    }

    public Date expired(int expired) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, expired);
        return calendar.getTime();
    }

}
