package cn.procsl.ping.boot.user;

import java.io.Serializable;

public interface User extends Serializable {

    String getName();

    Gender getGender();

    String getRemark();


    enum Gender {
        man, woman, unknown, unset
    }

}
