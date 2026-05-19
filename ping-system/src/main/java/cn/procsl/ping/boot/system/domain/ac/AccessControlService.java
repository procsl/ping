package cn.procsl.ping.boot.system.domain.ac;


import org.jspecify.annotations.NonNull;

/**
 * 访问控制服务
 */
public interface AccessControlService {

    @NonNull
    Effect matcher(@NonNull Request request, @NonNull Resource resource) throws AccessControlException;

}
