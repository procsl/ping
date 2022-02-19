package cn.procsl.ping.boot.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {

    String userName;
    String gender;
    String accountName;
    String password;

}
