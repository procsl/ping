package cn.procsl.ping.boot.jpa.domain.id;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Slf4j
@Repository
@RequiredArgsConstructor
class IdentifierRepositoryImpl implements IdentifierSegmentRepository {

    final EntityManager entityManager;

    @Override
    public Optional<Long> incrementBy(String segmentName, int size) {
        Identifier identifier = this.entityManager.find(Identifier.class, segmentName);
        if (identifier == null) {
            return Optional.empty();
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaUpdate<Identifier> updater = builder.createCriteriaUpdate(Identifier.class);

        Path<Long> valueField = updater.getRoot().get("value");
        Path<Long> segmentFiled = updater.getRoot().get("id");
        CriteriaUpdate<Identifier> command = updater.set(valueField, identifier.getValue() + size)
                .where(builder.equal(valueField, identifier.getValue()))
                .where(builder.equal(segmentFiled, segmentName));

        int result = entityManager.createQuery(command).executeUpdate();
        if (result >= 1) {
            return Optional.of(identifier.getValue() + size);
        }
        return Optional.empty();
    }

    @Override
    public void save(String segmentName, Long idValue) {
        String sql = "insert into ping_sequence (next_val, sequence_name) values (?, ?)";
        entityManager.createNativeQuery(sql, Identifier.class)
                .setParameter(1, idValue)
                .setParameter(2, segmentName)
                .executeUpdate();
    }

}
