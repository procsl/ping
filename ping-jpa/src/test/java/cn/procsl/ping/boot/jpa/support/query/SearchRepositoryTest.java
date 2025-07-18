package cn.procsl.ping.boot.jpa.support.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import javax.inject.Inject;
import java.util.List;


@Slf4j
@Rollback
@DataJpaTest
public class SearchRepositoryTest {


    @Inject
    EntityManager em;

    Long mainId;

    @BeforeEach
    void setUp() {
        // 插入主表
        MainEntity main = new MainEntity();
        main.setName("TestMain");
        main.setDesc("BeforeEach init data");
        em.persist(main);

        // 插入子表
        SubEntity sub1 = new SubEntity("Sub A");
        sub1.setDesc("desc A");
        sub1.setMainId(main.getId());
        em.persist(sub1);

        SubEntity sub2 = new SubEntity("Sub B");
        sub2.setDesc("desc B");
        sub2.setMainId(main.getId());
        em.persist(sub2);

        em.flush();  // 强制执行 SQL

        mainId = main.getId();
    }

    @Test
    public void test() {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<SubEntity> sub = cq.from(SubEntity.class);

        // 显式构造 join 条件
        Root<MainEntity> main = cq.from(MainEntity.class);
        cq.where(cb.equal(sub.get("mainId"), main.get("id")));

        cq.multiselect(main.get("name").alias("mainName"), sub.get("name").alias("subName"));

        List<Tuple> result = em.createQuery(cq).getResultList();
        log.info("result: {}", result);
    }

    @Test
    void testGroupByMainIdCount() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SubCountDTO> cq = cb.createQuery(SubCountDTO.class);

        Root<SubEntity> sub = cq.from(SubEntity.class);

        Path<Long> mainIdPath = sub.get("mainId");
        Expression<Long> countExpr = cb.count(sub.get("id"));

        // SELECT new SubCountDTO(mainId, COUNT(id)) GROUP BY mainId
        cq.select(cb.construct(SubCountDTO.class, mainIdPath, countExpr))
            .groupBy(mainIdPath);

        List<SubCountDTO> result = em.createQuery(cq).getResultList();

        for (SubCountDTO dto : result) {
            log.info("mainId: {}, count: {}", dto.mainId(), dto.count());
        }
    }

    public record SubCountDTO(Long mainId, Long count) {
    }


}
