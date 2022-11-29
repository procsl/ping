package cn.procsl.ping.boot.captcha.domain;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.springframework.data.domain.Persistable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Captcha implements Serializable, Persistable<Long> {

    final public static int max_times = 3;
    final protected static char[] number_chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    @Column(updatable = false, length = 50)
    protected String target;

    @Column(updatable = false, length = 20)
    protected String ticket;

    @Column(updatable = false)
    protected Date expiredDate;

    @Column(updatable = false)
    protected Date createDate;

    protected Integer times;

    protected Boolean verified;

    public Captcha(@NonNull String target, @NonNull String ticket, int expiredMinute) {
        this.target = target;
        this.ticket = ticket;
        this.expiredDate = this.calcExpiredDate(expiredMinute);
        this.createDate = new Date();
        this.times = 0;
        this.verified = false;
    }

    @SuppressWarnings("all")
    protected static String random(int count, char[] chars) {
        Random random = new Random();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(chars.length);
            buffer.append(chars[index]);
        }
        return buffer.toString();
    }

    public void verify(@NonNull String ticket) throws VerifyFailureException {
        // 如果当前时间是过期时间之后, 则属于验证码超时
        if (new Date().after(expiredDate)) {
            throw new VerifyFailureException("%s验证码已过期", this.message());
        }

        // 如果已经被使用过, 则报错
        if (this.verified) {
            throw new VerifyFailureException("%s验证码已失效", this.message());
        }

        // 超过最大错误次数
        if (this.times >= max_times) {
            throw new VerifyFailureException("%s验证码已失效", this.message());
        }
        this.times++;
        boolean bool = this.check(ticket);
        if (!bool) {
            throw new VerifyFailureException(true, "%s验证码错误", this.message());
        }
        this.verified = true;
    }

    protected boolean check(String ticket) throws VerifyFailureException {
        return ObjectUtils.nullSafeEquals(ticket, this.getTicket());
    }

    public abstract String message();

    protected Date calcExpiredDate(int expired) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, expired);
        return calendar.getTime();
    }

    @Override
    public boolean isNew() {
        return this.getId() == null;
    }

    @Override
    public @NonNull String toString() {
        SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
        return "{" + "target='" + target + '\'' + ", ticket='" + ticket + '\'' + ", expired=" + format.format(
                expiredDate) + ", " + "createDate=" + format.format(createDate) + ", id=" + getId() + '}';
    }
}
