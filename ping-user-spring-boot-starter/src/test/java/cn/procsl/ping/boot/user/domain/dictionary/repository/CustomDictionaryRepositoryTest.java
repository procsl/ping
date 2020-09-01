package cn.procsl.ping.boot.user.domain.dictionary.repository;

import com.mysema.commons.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static cn.procsl.ping.boot.user.domain.dictionary.repository.CustomDictionaryRepository.dict;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CustomDictionaryRepositoryTest {

    @Inject
    CustomDictionaryRepository cusRepo;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void search() {

        Assert.notNull(cusRepo, "CustomDictionaryRepository not null");

        Long id = cusRepo.search(dict.id, "/root", true);
        log.info("id={}", id);
    }
}
