package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.domain.rbac.entity.Resource;
import cn.procsl.ping.boot.user.domain.rbac.entity.ResourceType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.util.Collections;

/**
 * @author procsl
 * @date 2020/07/06
 */
@Slf4j
@Transactional
@SpringBootTest
@RunWith(SpringRunner.class)
public class ResourceServiceTest {

    @Inject
    ResourceService resourceService;

    private Long publicResourceId;

    @Before
    public void before() {
        publicResourceId = resourceService.create("资源名称", ResourceType.PAGE, null, null);
    }

    @Test
    public void create() {
        Long id = resourceService.create("测试资源", ResourceType.PAGE, null, null);
        Assert.assertNotNull(id);

        Long id1 = resourceService.create("测试资源1", ResourceType.PAGE, id, null);
        Assert.assertNotNull(id1);

        Long id2 = resourceService.create("测试资源2", ResourceType.PAGE, id, Collections.singleton(id1));
        Assert.assertNotNull(id2);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createCase1() {
        resourceService.create(null, ResourceType.PAGE, null, null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createCase2() {
        resourceService.create("", ResourceType.PAGE, null, null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createCase3() {
        resourceService.create("数数据数据数据数据数据数据数据数据数据数据数据数据数据数据数据数据数据数据数据数",
                ResourceType.PAGE, null, null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createCase4() {
        resourceService.create("数据",
                null, null, null);
    }

    @Test(expected = BusinessException.class)
    public void delete() {

        Long id = resourceService.create("测试资源", ResourceType.PAGE, null, null);
        Assert.assertNotNull(id);

        resourceService.delete(id);

        Resource resource = resourceService.load(id);
        Assert.assertNull(resource);
    }

    @Test
    public void load() {
        Resource resource = resourceService.load(this.publicResourceId);

        Assert.assertNotNull(resource);
    }

    @Test
    public void changeType() {
        resourceService.changeType(publicResourceId, ResourceType.BUTTON);

        Resource resource = resourceService.load(this.publicResourceId);
        Assert.assertEquals(ResourceType.BUTTON, resource.getType());
    }

    @Test
    public void changeParent() {

        Long parent = resourceService.create("parent", ResourceType.PAGE, null, null);

        resourceService.changeParent(publicResourceId, parent);

        Resource resource = resourceService.load(this.publicResourceId);
        Assert.assertEquals(resource.getNode().getParentId(), parent);
    }

    @Test
    public void changeName() {
        String changedName = "changechange";
        resourceService.changeName(publicResourceId, changedName);

        Resource resource = resourceService.load(publicResourceId);

        Assert.assertEquals(resource.getName(), changedName);
    }


    @Test
    public void existsName() {
        boolean eq = resourceService.existsName(1000L, "资源名称");
        Assert.assertTrue(eq);
    }

    @Test
    public void existsNameCase1() {
        boolean eq = resourceService.existsName(publicResourceId, "资源名称1");
        Assert.assertFalse(eq);
    }

    @Test
    public void exists() {
        resourceService.exists(publicResourceId);
    }

    @Test(expected = BusinessException.class)
    public void existsCase1() {
        resourceService.exists(100L);
    }
}
