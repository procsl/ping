package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.TestJpaApplication;
import cn.procsl.ping.boot.jpa.support.query.ProjectionDTO;
import cn.procsl.ping.boot.jpa.support.query.ProjectionSearchRepository;
import cn.procsl.ping.boot.jpa.support.query.ast.domain.ResultDTO;
import cn.procsl.ping.boot.jpa.support.query.ast.domain.SingletonQueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Service
@Validated
@Transactional
@Rollback
@SpringBootTest(classes = TestJpaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProjectionSearchRepositoryImplTest {

    @Inject
    ProjectionSearchRepository projectionSearchRepository;

    @Test
    public void search() {
        SingletonQueryDTO query = new SingletonQueryDTO();
        query.setId(1L);
        query.setTeacherName("test");
        List<ResultDTO> result = this.projectionSearchRepository.search(query, ResultDTO.class);
        log.debug("结果: {}", result);
    }
}
