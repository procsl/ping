package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.TestJpaApplication;
import cn.procsl.ping.boot.jpa.support.query.MainEntity;
import cn.procsl.ping.boot.jpa.support.query.ProjectionDTO;
import cn.procsl.ping.boot.jpa.support.query.ProjectionSearchRepository;
import cn.procsl.ping.boot.jpa.support.query.SubEntity;
import cn.procsl.ping.boot.jpa.support.query.ast.domain.ResultVO;
import cn.procsl.ping.boot.jpa.support.query.ast.domain.SingletonQueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;

import java.util.List;

@Slf4j
@Service
@Validated
@Transactional
@Rollback
@SpringBootTest(classes = TestJpaApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProjectionSearchRepositoryImplTest {

    @Inject
    ProjectionSearchRepository projectionSearchRepository;

    @Inject
    JpaRepository<MainEntity, Long> mainEntityLongJpaRepository;

    @Inject
    JpaRepository<SubEntity, Long> subEntityLongJpaRepository;


    @Test
    public void search() {
        ProjectionDTO pp = new ProjectionDTO();
        pp.setName("2");

        MainEntity entity = new MainEntity();
        entity.setName("1");
        entity.setDesc("你好啊");
        entity = this.mainEntityLongJpaRepository.save(entity);

        for (int i = 0; i < 10; i++) {
            SubEntity sub = new SubEntity();
            sub.setMainId(entity.getId());
            sub.setName("我是sub" + i);
            sub.setDesc("我是描述" + i);
            this.subEntityLongJpaRepository.save(sub);
        }
        this.subEntityLongJpaRepository.flush();
        List<ResultVO> result = this.projectionSearchRepository.search(pp, ResultVO.class);
        log.info("结果集: {}", result);
    }
}
