package cn.procsl.ping.boot.data.business.entity;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author procsl
 * @date 2020/05/14
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@Transactional(rollbackOn = Exception.class)
public class EntitySaveTest {

    @Inject
    JpaRepository<GeneralEntityTest, String> jpaRepository;


    @Test
    public void generalEntityTest() {
        GeneralEntityTest entity = new GeneralEntityTest();
        entity.setName("名称");
        jpaRepository.save(entity);
        TreeNodeTest newNode = TreeNodeTest.root.create(entity.getId());
        entity.setNodeTest(newNode);
        jpaRepository.save(entity);
        jpaRepository.flush();
    }


    @Test(expected = DataIntegrityViolationException.class)
    public void lengthTest() {
        GeneralEntityTest entity = new GeneralEntityTest();
        entity.setName("名称");
        jpaRepository.save(entity);
        TreeNodeTest newNode = TreeNodeTest.root.create(entity.getId());
        newNode.setParentId("11111111111111111111111111111111111111111111111111111111111" +
                "11111111111111111111111111111111111111111111111111111111111");
        entity.setNodeTest(newNode);
        jpaRepository.save(entity);
        jpaRepository.flush();
    }

}
