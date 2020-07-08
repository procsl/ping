package cn.procsl.ping.boot.user.domain.rbac.entity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author procsl
 * @date 2020/07/08
 */
public class RelationTest {

    private List<Long> roles;
    private Relation relation;

    @Before
    public void before() {
        this.roles = Arrays.asList(1L, 2L, 3L);
        this.relation = Relation.create("name", RelationType.EXCLUDE, "script", roles);
    }

    @Test
    public void changeRoles() {
        this.relation.changeRoles(roles);
        Assert.assertTrue(this.relation.getRoles().containsAll(roles));
    }

    @Test(expected = NullPointerException.class)
    public void changeRolesCase() {
        this.relation.changeRoles(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void changeRolesCase1() {
        this.relation.changeRoles(Collections.EMPTY_SET);
    }

    @Test
    public void changeScript() {
        this.relation.changeScript("1234");
        Assert.assertTrue(this.relation.getScript().equals("1234"));
    }

    @Test
    public void changeScriptCase() {
        this.relation.changeScript(null);
    }

    @Test
    public void changeScriptCase2() {
        this.relation.changeScript("");
    }


    @Test
    public void changeName() {
        this.relation.changeName("1234");
        Assert.assertTrue(this.relation.getName().equals("1234"));
    }

    @Test(expected = NullPointerException.class)
    public void changeNameCase() {
        this.relation.changeName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void changeNameCase1() {
        this.relation.changeName("");
    }

    @Test(expected = NullPointerException.class)
    public void changeType() {
        this.relation.changeType(null);
    }

    @Test
    public void changeTypeCase() {
        this.relation.changeType(RelationType.ROLE_BASE);
        boolean bool = this.relation.getType().equals(RelationType.ROLE_BASE);
        Assert.assertTrue(bool);
    }
}
