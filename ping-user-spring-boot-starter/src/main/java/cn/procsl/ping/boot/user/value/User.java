package cn.procsl.ping.boot.user.value;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {

    String name;

    String accountName;
}
