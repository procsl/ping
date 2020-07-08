package cn.procsl.ping.boot.user.domain.rbac.entity;

/**
 * @author procsl
 * @date 2020/07/08
 */
public enum Operation {

    READ_ONLY("只读"),

    ENABLE("启用"),

    DISABLE("禁用"),

    WRITEABLE("可读写");

    private final String name;

    Operation(String s) {
        this.name = s;
    }

    public String getName() {
        return name;
    }
}
