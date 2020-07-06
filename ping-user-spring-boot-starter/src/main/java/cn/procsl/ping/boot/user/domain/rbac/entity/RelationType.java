package cn.procsl.ping.boot.user.domain.rbac.entity;

/**
 * @author procsl
 * @date 2020/07/05
 */
public enum RelationType {

    REQUIRED("前置约束"),

    EXCLUDE("互斥约束"),

    ROLE_BASE("角色基数约束"),

    USER_BASE("用户基数约束");

    private final String desc;

    RelationType(String desc) {
        this.desc = desc;
    }
}
