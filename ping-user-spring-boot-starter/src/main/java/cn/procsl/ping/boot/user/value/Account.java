package cn.procsl.ping.boot.user.value;

import lombok.Data;

import java.io.Serializable;

@Data
public class Account implements Serializable {
    String name;

    String password;

    String type;
}
