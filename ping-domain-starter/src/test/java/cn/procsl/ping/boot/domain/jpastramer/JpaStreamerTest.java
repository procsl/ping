package cn.procsl.ping.boot.domain.jpastramer;

import cn.procsl.ping.boot.domain.DomainApplication;
import com.github.jsonzou.jmockdata.JMockData;
import com.github.jsonzou.jmockdata.MockConfig;
import com.speedment.jpastreamer.application.JPAStreamer;
import com.speedment.jpastreamer.projection.Projection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@SpringBootTest(classes = DomainApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Rollback
@Transactional
@Configuration(proxyBeanMethods = false)
public class JpaStreamerTest {

    @Autowired
    JPAStreamer jpaStreamer;

    @Autowired
    JpaRepository<Streamer, Long> jpaRepository;


    @BeforeEach
    @Rollback(value = false)
    public void setUp() {
        log.info("开始测试:setUp");
        MockConfig config = new MockConfig().globalConfig().excludes("id");
        ArrayList<Streamer> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Streamer data = JMockData.mock(Streamer.class, config);
            list.add(data);
            log.info("实体:{}", data);
        }
        jpaRepository.saveAll(list);
        List<Streamer> all = jpaRepository.findAll();
        Assertions.assertNotEquals(0, all.size());
        log.info("结束:setUp");
    }

    @Test
    @DisplayName("Streamer单元测试")
    @Transactional
    public void test() {
        log.info("开始测试");
        {
            List<Streamer> all = jpaRepository.findAll();
            Assertions.assertNotEquals(0, all.size());
        }

        {
            log.info("Streamer查询所有数据:开始");
            List<Streamer> all = jpaStreamer.stream(Streamer.class).collect(Collectors.toList());
            log.info("Streamer查询所有数据:结束");
            all.forEach(item -> log.info("当前对象:[{}]", item));
            Assertions.assertNotEquals(0, all.size());
        }
        {
            log.info("Stream条件查询:开始");
            Projection<Streamer> select = Projection.select(Streamer$.name, Streamer$.collection);
            List<Streamer> result = jpaStreamer
                    .stream(select)
                    .filter(Streamer$.name.contains("a"))
                    .sorted(Streamer$.type)
                    .limit(10).skip(5)
                    .collect(Collectors.toList());
            result.forEach(item -> log.info("匹配的数据:{}", item));
            log.info("Stream条件查询:结束");
        }

        {
            log.info("Stream 查询部分字段:开始");
            jpaStreamer.stream(Streamer.class).map(item -> {
                HashMap<String, Types> hashmap = new HashMap<>(1);
                hashmap.put(item.getName(), item.getType());
                return hashmap;
            }).limit(10).forEach(item -> log.info("数据:{}", item));
            log.info("Stream 查询部分字段:结束");
        }
        log.info("测试结束");
    }
}
