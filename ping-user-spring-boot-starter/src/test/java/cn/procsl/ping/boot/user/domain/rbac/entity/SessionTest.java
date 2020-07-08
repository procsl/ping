package cn.procsl.ping.boot.user.domain.rbac.entity;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_SET;

/**
 * @author procsl
 * @date 2020/07/08
 */
@Slf4j
public class SessionTest {

    private Set<Long> roles;
    private Session sesssion0;
    private Session sesssion1;

    @Before
    public void before() {
        this.sesssion0 = new Session(true);
        this.roles = Arrays.asList(1L, 2L, 3L).stream().collect(Collectors.toSet());
        this.sesssion1 = new Session(false, roles);
    }

    @Test
    public void init() {
        new Session(true);

        new Session(true, null);

        new Session(false, null);

        new Session(false, EMPTY_SET);

        new Session(true, Collections.singleton(1L));

        new Session(true, roles);
    }

    @Test(expected = NullPointerException.class)
    public void addRole() {
        this.sesssion0.addRole(null);
    }

    @Test
    public void addRoleCase1() {
        this.sesssion0.addRole(1L);
    }

    @Test
    public void addRoleCase2() {
        this.sesssion0.addRole(-1L);
    }

    @Test(expected = NullPointerException.class)
    public void remove() {
        this.sesssion0.remove(null);
    }

    @Test
    public void removeCase1() {
        this.sesssion0.remove(1L);
    }

    @Test
    public void removeCase2() {
        this.sesssion0.remove(-1L);
    }

    @Test
    public void hasRole() {
        Assert.assertFalse(this.sesssion0.hasRole(0L));
        Assert.assertFalse(this.sesssion0.hasRole(1L));
        Assert.assertFalse(this.sesssion0.hasRole(2L));

        Assert.assertTrue(this.sesssion1.hasRole(1L));
        Assert.assertTrue(this.sesssion1.hasRole(2L));
        Assert.assertTrue(this.sesssion1.hasRole(3L));
    }

    @Test
    public void enable() {
        Assert.assertTrue(this.sesssion0.isActive());

        sesssion0.enable();
        Assert.assertTrue(this.sesssion0.isActive());

        Assert.assertFalse(this.sesssion1.isActive());
        sesssion1.enable();
        Assert.assertTrue(this.sesssion1.isActive());
    }

    @Test
    public void disable() {
        Assert.assertTrue(this.sesssion0.isActive());

        sesssion0.disable();
        Assert.assertFalse(this.sesssion0.isActive());

        Assert.assertFalse(this.sesssion1.isActive());
        sesssion1.disable();
        Assert.assertFalse(this.sesssion1.isActive());
    }
}
