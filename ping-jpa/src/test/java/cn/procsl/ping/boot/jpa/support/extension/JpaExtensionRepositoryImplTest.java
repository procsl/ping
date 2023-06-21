package cn.procsl.ping.boot.jpa.support.extension;

import cn.procsl.ping.boot.jpa.TestJpaApplication;
import cn.procsl.ping.boot.jpa.domain.ExtensionRepository;
import cn.procsl.ping.boot.jpa.domain.TestEntity;
import com.github.jsonzou.jmockdata.JMockData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.projection.EntityProjection;
import org.springframework.data.projection.ProjectionInformation;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.util.TypeInformation;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Validated
@Transactional
@Rollback
@SpringBootTest(classes = TestJpaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class JpaExtensionRepositoryImplTest {

    @Inject
    ExtensionRepository extensionRepository;

    @Inject
    JpaRepository<TestEntity, Long> jpaRepository;

    @Inject
    EntityManager entityManager;

    @RepeatedTest(10)
    public void test2() {
        jpaRepository.save(JMockData.mock(TestEntity.class));
        List<TestEntity> test = jpaRepository.findAll();
        log.info("test: {}", test);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> tupleQuery = builder.createTupleQuery();
        Root<TestEntity> root = tupleQuery.from(TestEntity.class);

        CriteriaQuery<Tuple> select = tupleQuery.multiselect(root.get("name"), root.get("id"), root.get("auditable").get("createdBy")).where(builder.equal(root.get("name"), "lp"));

        List<Tuple> result = entityManager.createQuery(select).getResultList();
        log.info("result: {}", result);
    }

    @Test
    public void test() {

        List<TestEntity> all = this.jpaRepository.findAll();
        log.info("all: {}", all);

        all = extensionRepository.findAllBy();
        log.info("all: {}", all);
    }

    @Test
    public void setJpaRepository() {

//        TypeInformation
        SpelAwareProxyProjectionFactory spelAwareProxyProjectionFactory = new SpelAwareProxyProjectionFactory();
        ProjectionInformation entity = spelAwareProxyProjectionFactory.getProjectionInformation(TestEntity.class);
        TypeInformation<TestEntity> type = TypeInformation.of(TestEntity.class);

        EntityProjection<TestEntity, TestEntity> projection = EntityProjection.projecting(type, type, null, EntityProjection.ProjectionType.DTO);
        List<Optional<String>> result = projection.map(item -> Optional.of("")).stream().toList();
    }

}