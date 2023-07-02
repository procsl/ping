package cn.procsl.ping.boot.jpa.domain;

import cn.procsl.ping.boot.jpa.support.extension.JpaExtensionRepository;
import cn.procsl.ping.boot.jpa.support.extension.Selection;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Repository;

import java.util.List;

@Indexed
@Repository
public interface ExtensionRepository extends JpaExtensionRepository<TestEntity, Long> {

    List<TestEntity> findAllBy();

    @Selection(projection = TestDTO.class)
    default Specification<TestEntity> findBySpec(Root<TestEntity> root, CriteriaQuery<Tuple> query, CriteriaBuilder cb) {
        return null;
    }

}
