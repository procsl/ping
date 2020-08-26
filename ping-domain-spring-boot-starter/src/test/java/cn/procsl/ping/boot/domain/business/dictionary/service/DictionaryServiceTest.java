package cn.procsl.ping.boot.domain.business.dictionary.service;

import cn.procsl.ping.boot.domain.business.dictionary.model.DictValueDTO;
import cn.procsl.ping.boot.domain.business.dictionary.model.Type;
import cn.procsl.ping.boot.domain.business.dictionary.model.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@SpringBootApplication(scanBasePackages = "cn.procsl.ping.boot.domain")
@Rollback(value = false)
public class DictionaryServiceTest {

    @Inject
    DictionaryService dictionaryService;

    private Long rootId;

    @Before
    public void setUp() {
        rootId = dictionaryService.create("/root");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void create() {
        Long id = dictionaryService.create(rootId, "profile");
        log.info("create id:{}", id);
    }

    @Test
    public void testCreate() {
        dictionaryService.create("test");
    }

    @Test
    public void rename() {
        String name = this.dictionaryService.rename(rootId, "new-root");
        log.info("ole name:{}", name);
    }

    @Test
    public void remove() {
        int count = this.dictionaryService.remove(rootId);
        log.info("count:{}", count);
    }

    @Test
    public void disable() {
        this.dictionaryService.disable(rootId);
    }

    @Test
    public void enable() {
        this.dictionaryService.enable(rootId);
    }

    @Test
    public void changeValue() {
        Value value = new Value(Type.integer, "120");
        this.dictionaryService.changeValue(rootId, value);
    }

    @Test
    public void search() {
        DictValueDTO values = this.dictionaryService.search("/root");
        Assert.assertNull(values);
    }

    @Test
    public void contains() {
    }
}
