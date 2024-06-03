package cn.procsl.ping.boot.jpa.domain.id;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
public class IdentifierSegmentRepositoryImpl implements IdentifierSegmentRepository {

    final EntityManager entityManager;

    @NonNull
    @Override
    public Optional<Long> incrementBy(String segmentName, int size) throws IdentifierException {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<InnerIdentifier> qr = query.from(InnerIdentifier.class);

        query = query.multiselect(qr.get("value").alias("next_value")).where(builder.equal(qr.get("id"), segmentName));

        List<Tuple> list = entityManager.createQuery(query).getResultList();
        if (list.isEmpty()) {
            throw new IdentifierException(segmentName + " not found");
        }

        Tuple identifier = list.getFirst();
        CriteriaUpdate<InnerIdentifier> updater = builder.createCriteriaUpdate(InnerIdentifier.class);
        Path<Long> valueField = updater.getRoot().get("value");
        Path<String> segmentFiled = updater.getRoot().get("id");

        Long nextValue = identifier.get("next_value", Long.class);
        CriteriaUpdate<InnerIdentifier> command = updater.set(valueField, nextValue + size)
                .where(builder.equal(valueField, nextValue), builder.equal(segmentFiled, segmentName));

        int result = entityManager.createQuery(command).executeUpdate();
        if (result >= 1) {
            return Optional.of(nextValue + size);
        }
        return Optional.empty();
    }

    @Override
    public void save(String segmentName, Long initValue) {
        this.entityManager.persist(new InnerIdentifier(segmentName, initValue));
        this.entityManager.flush();
    }

}
