package cn.procsl.ping.boot.system.domain.secret;

import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@RepositoryCreator
@Table(name = "s_private_key")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrivateKey implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    Long id;

    @Column(length = 36, updatable = false)
    String private_key;

    /**
     * 开始生效时间
     */
    @Column(updatable = false)
    Date forceDate;

    /**
     * 过期时间
     */
    @Column(updatable = false)
    Date expiredDate;

    /**
     * @param forceDate 开始生效时间
     * @return 返回生成的私钥
     */
    public static PrivateKey newKey(@NonNull Date forceDate) {

        PrivateKey privateKey = new PrivateKey();
        UUID uuid = UUID.randomUUID();
        privateKey.setPrivate_key(uuid.toString());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(forceDate);

        calendar.add(Calendar.DATE, 3);

        privateKey.setExpiredDate(calendar.getTime());
        privateKey.setForceDate(forceDate);

        return privateKey;

    }

    @Override
    public String toString() {

        SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");

        return "PrivateKey{" +
                "id=" + id +
                ", private_key='" + private_key + '\'' +
                ", forceDate=" + format.format(forceDate) +
                ", expiredDate=" + format.format(expiredDate) +
                '}';
    }
}
