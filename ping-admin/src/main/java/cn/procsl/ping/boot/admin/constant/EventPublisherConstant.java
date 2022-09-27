package cn.procsl.ping.boot.admin.constant;


public interface EventPublisherConstant {

    String ROLE_CHANGED_EVENT = "cn.procsl.ping.admin.role.changed";

    String ROLE_CREATE_EVENT = "cn.procsl.ping.admin.role.created";

    String ROLE_DELETE_EVENT = "cn.procsl.ping.admin.role.deleted";
    String PERMISSION_CREATE_EVENT = "cn.procsl.ping.admin.permission.created";
    String PERMISSION_DELETE_EVENT = "cn.procsl.ping.admin.permission.deleted";
    String PERMISSION_UPDATE_EVENT = "cn.procsl.ping.admin.permission.changed";

    String USER_LOGOUT = "cn.procsl.ping.admin.user.logout";

    String USER_LOGIN = "cn.procsl.ping.admin.user.login";
}
