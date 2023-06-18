package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.domain.user.AccountState;

public interface UserAccountRecord {

    String getName();


    AccountState getState();

}
