package cn.procsl.ping.boot.admin.domain.rbac;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class SubjectTest {

    @Test
    void putRoles() {

        Subject sub = new Subject();
        sub.putRoles(List.of(new Role(), new Role(), new Role(), new Role()));
        Assertions.assertNotNull(sub.getRoles());
        Assertions.assertEquals(4, sub.getRoles().size());

    }
}