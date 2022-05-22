package cn.procsl.ping.boot.infra.domain.user;

public interface AuthenticateService {

    Long create(String account, String password);

    Long authenticate(String account, String password);
}
