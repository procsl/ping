package cn.procsl.ping.boot.jpa.support.query;

import cn.procsl.ping.boot.jpa.TestJpaApplication;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;


@Slf4j
@Service
@Validated
@Transactional
@Rollback
@SpringBootTest(classes = TestJpaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ComposeQueryRepositoryTest {

    @Inject
    ComposeQueryRepository composeQueryRepository;

    @Inject
    JpaRepository<MainEntity, Long> jpaRepository;

    @Inject
    EntityManager em;

    @Test
    public void queryProjections() {

        {
            for (int i = 0; i < 100; i++) {
                MainEntity entity = new MainEntity();
                entity.setName("朝闻道_%s".formatted(i));
                entity.setDesc("描述_%s".formatted(i));
                jpaRepository.save(entity);

                SubEntity sub = new SubEntity();
                sub.setMainId(entity.getId());
                sub.setDesc("sub desc_%s".formatted(i));
                sub.setName("sub name_%s".formatted(i));

                SubEntity sub2 = new SubEntity();
                sub2.setMainId(entity.getId());
                sub2.setDesc("sub desc_%s".formatted(i + 1));
                sub2.setName("sub name_%s".formatted(i + 1));
            }
        }
        jpaRepository.flush();

        {
            log.info("JPQL查询开始");
            String jpql = "select new cn.procsl.ping.boot.jpa.support.query.ProjectionDTO(e.id, null ) as aa, " +
                " new cn.procsl.ping.boot.jpa.support.query.ProjectionDTO(1L , e.name ) as bb from MainEntity e " +
                "inner join SubEntity as s on e.id = s.mainId where e.name like :name";
            TypedQuery<ProjectionDTO> query = em.createQuery(jpql, ProjectionDTO.class);
            query.setParameter("name", "朝闻道_%");
            List<?> result = query.getResultList();
            log.info("JPQL查询完成: {}", result);
        }

        ProjectionDTO project = new ProjectionDTO();
        project.setName("朝闻道%");
        Collection<ProjectionDTO> result = composeQueryRepository.query(project);
        log.info("结果{}", result);
    }
}
