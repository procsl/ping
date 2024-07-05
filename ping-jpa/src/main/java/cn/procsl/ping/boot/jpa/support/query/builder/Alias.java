package cn.procsl.ping.boot.jpa.support.query.builder;

public interface Alias {

    /**
     * 获取别名,即 xx as alias
     */
    default String getAliasName() {
        return null;
    }

    /**
     * 获取原始名称
     */
    default String getOriginName() {
        return null;
    }

}
