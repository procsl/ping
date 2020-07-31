package cn.procsl.ping.boot.domain.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author procsl
 * @date 2020/04/27
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DataAutoConfigurationTest {

    @Inject
    Optional<DataAutoConfiguration> dataAutoConfiguration;

    @Test
    public void test() {
        Assert.assertNotNull("自动配置类测试失败", dataAutoConfiguration.get());
    }
}
