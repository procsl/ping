package cn.procsl.ping.boot.user.service.res;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.command.res.ChangeNameCommand;
import cn.procsl.ping.boot.user.command.res.ChangeParentCommand;
import cn.procsl.ping.boot.user.command.res.ChangeTypeCommand;
import cn.procsl.ping.boot.user.command.res.CreateCommand;
import cn.procsl.ping.boot.user.domain.res.entity.Resource;
import cn.procsl.ping.boot.user.domain.res.entity.ResourceType;
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

/**
 * @author procsl
 * @date 2020/07/09
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
public class ResourceServiceTest {

    @Inject
    ResourceService resourceService;

    private Long resourceId;
    private Long resourceId1;

    @Before
    public void before() {
        CreateCommand command = new CreateCommand("资源", ResourceType.BUTTON, null, null);
        resourceId = resourceService.create(command);

        CreateCommand command1 = new CreateCommand("资源1", ResourceType.BUTTON, null, null);
        resourceId1 = resourceService.create(command1);

    }

    @Test(expected = BusinessException.class)
    public void create() {
        CreateCommand command = new CreateCommand("资源", ResourceType.BUTTON, null, null);
        resourceService.create(command);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createCase() {
        CreateCommand command = new CreateCommand("", ResourceType.BUTTON, null, null);
        resourceService.create(command);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createCase1() {
        CreateCommand command = new CreateCommand(null, ResourceType.BUTTON, null, null);
        resourceService.create(command);
    }

    @Test(expected = ConstraintViolationException.class)
    public void createCase2() {
        CreateCommand command = new CreateCommand("name", null, null, null);
        resourceService.create(command);
    }

    @Test(expected = BusinessException.class)
    public void delete() {
        resourceService.delete(this.resourceId);
        resourceService.load(this.resourceId);
    }

    @Test
    public void changeType() {
        ChangeTypeCommand command = new ChangeTypeCommand(resourceId, ResourceType.OTHER);
        resourceService.changeType(command);

        Resource resource = resourceService.load(resourceId);
        Assert.assertEquals(ResourceType.OTHER, resource.getType());
    }

    @Test
    public void changeParent() {
        ChangeParentCommand command = new ChangeParentCommand(resourceId, resourceId1);
        resourceService.changeParent(command);

        Resource resource = resourceService.load(resourceId);
        Assert.assertEquals(resource.getNode().getParentId(), resourceId1);
    }

    @Test
    public void changeName() {
        ChangeNameCommand command = new ChangeNameCommand(resourceId, "New name");
        resourceService.changeName(command);

        Resource resource = resourceService.load(resourceId);
        Assert.assertEquals(resource.getName(), "New name");
    }

    @Test(expected = BusinessException.class)
    public void checkNameable() {
        ChangeNameCommand command = new ChangeNameCommand(resourceId, "资源1");
        resourceService.checkNameable(command);
    }

    @Test(expected = BusinessException.class)
    public void checkNameableCase() {
        ChangeNameCommand command = new ChangeNameCommand(resourceId, "资源1");
        resourceService.checkNameable(command);
    }

    @Test(expected = BusinessException.class)
    public void testCheckNameable() {
        resourceService.checkNameable("资源");
    }
}
