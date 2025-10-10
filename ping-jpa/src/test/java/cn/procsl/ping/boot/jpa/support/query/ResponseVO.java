package cn.procsl.ping.boot.jpa.support.query;

import java.io.Serializable;

public class ResponseVO implements Serializable {


    Long id;

    String name;

    @ReferenceBy(ref = "sub")
    String desc;

}
