package cn.procsl.ping.boot.infra.user;

public interface AccountFacade {

    Long create(String account, String password);

    Long loadBy(String account, String password);
}
