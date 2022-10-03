package cn.procsl.ping.boot.admin.domain.rbac;

import lombok.Data;

/**
 * 数据权限
 */
@Data
public class DataPermission extends Permission {

    String resource;

    String operate;

    @Override
    public boolean readOnly() {
        return true;
    }
}
