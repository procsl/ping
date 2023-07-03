package cn.procsl.ping.boot.system.api.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
@EqualsAndHashCode
@Getter
@Setter
public class AuthenticateVO {

    String account;

    String nickName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Date loginDate;

}
