package cn.procsl.ping.boot.user.domain.rbac.entity;

import org.junit.Assert;
import org.junit.Test;

import static cn.procsl.ping.boot.user.domain.rbac.entity.Operation.READ_ONLY;

/**
 * @author procsl
 * @date 2020/07/08
 */
public class PermissionTest {

    @Test
    public void create() {
        Permission p1 = Permission.create(1L, READ_ONLY, "name");
        Assert.assertNotNull(p1);
        Assert.assertEquals(p1.getName(), "name");

        Permission p2 = Permission.create(1L, READ_ONLY, null);
        Assert.assertNotNull(p2);
        Assert.assertEquals(p2.getName(), READ_ONLY.getName());

        Permission p3 = Permission.create(1L, READ_ONLY, "");
        Assert.assertNotNull(p3);
        Assert.assertEquals(p3.getName(), READ_ONLY.getName());
    }
}
