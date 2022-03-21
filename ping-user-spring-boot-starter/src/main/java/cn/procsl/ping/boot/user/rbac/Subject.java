package cn.procsl.ping.boot.user.rbac;

import java.io.Serializable;
import java.util.Set;

public interface Subject<R extends Serializable> extends Serializable {

    Long getSubjectId();

    Set<R> getRoles();


}
