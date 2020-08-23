package cn.procsl.ping.boot.domain.domain.service;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.domain.entity.PathNode;
import cn.procsl.ping.boot.domain.domain.entity.TreeEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

/**
 * @author procsl
 * @date 2020/07/31
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TreeEntityRepositoryTest {

    @Inject
    AdjacencyTreeRepository<TreeEntity, Long, PathNode> adjacencyTreeExecutor;

    @Test
    public void test() {
        Assert.assertNotNull(adjacencyTreeExecutor);
    }


}
