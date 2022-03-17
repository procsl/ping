package cn.procsl.ping.boot.user.account;

import java.io.Serializable;

public interface Account extends Serializable {

    Long getUserId();

    String getName();

    String getPassword();

}
