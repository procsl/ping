package cn.procsl.ping.boot.user.domain.dictionary.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DictionaryServiceTest {

    @Inject
    DictionaryService dictionaryService;

    @Before
    public void setUp() throws Exception {
    }

    @Test(expected = IllegalArgumentException.class)
    public void create() {
        Long test = dictionaryService.create("/0/1/2/3/4/5/6/7/8/9/10/11/12/13", true);
        log.info("print this id:{}", test);
    }

    @Test
    public void testCreate() {
        Long test = dictionaryService.create("/0/1/2/12/13", true);
        log.info("print this id:{}", test);
    }

    @Test
    public void disable() {
        dictionaryService.disable(1L);
    }

    @Test
    public void enable() {
        dictionaryService.enable(1L);
    }

    @Test
    public void rename() {
        dictionaryService.rename(1L, "root");
    }

    @Test
    public void remove() {
        dictionaryService.remove(1L);
    }

}
