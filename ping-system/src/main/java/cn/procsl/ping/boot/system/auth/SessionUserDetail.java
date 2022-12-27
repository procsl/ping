package cn.procsl.ping.boot.system.auth;

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
public class SessionUserDetail {

    String account;

    String nickName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Date loginDate;

}
