package cn.procsl.ping.boot.jpa.domain.id;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Slf4j
public class TableIdentifierSegmentRepositoryImpl implements IdentifierSegmentRepository {

    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public TableIdentifierSegmentRepositoryImpl(@NonNull EntityManager entityManager, @NonNull PlatformTransactionManager txManager) {
        this.entityManager = entityManager;
        final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        def.setReadOnly(false);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        def.setTimeout(1000);
        this.transactionTemplate = new TransactionTemplate(txManager, def);
    }

    @NonNull
    @Override
    public Optional<Long> incrementBy(String segmentName, int size) throws IdentifierException {
        return Objects.requireNonNull(this.transactionTemplate.execute(status -> {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tuple> query = builder.createTupleQuery();
            Root<InnerIdentifier> qr = query.from(InnerIdentifier.class);

            final String nextValueKey = "next_value";

            query = query.multiselect(qr.get(InnerIdentifier_.VALUE).alias(nextValueKey))
                .where(builder.equal(qr.get(InnerIdentifier_.ID), segmentName));

            List<Tuple> list = entityManager.createQuery(query).getResultList();
            if (list.isEmpty()) {
                throw new IdentifierException(segmentName + " not found");
            }

            CriteriaUpdate<InnerIdentifier> updater = builder.createCriteriaUpdate(InnerIdentifier.class);
            Path<Long> valueField = updater.getRoot().get(InnerIdentifier_.VALUE);
            Path<String> segmentFiled = updater.getRoot().get(InnerIdentifier_.ID);

            Tuple identifier = list.getFirst();
            Long nextValue = identifier.get(nextValueKey, Long.class);
            CriteriaUpdate<InnerIdentifier> command = updater.set(valueField, nextValue + size)
                .where(builder.equal(valueField, nextValue), builder.equal(segmentFiled, segmentName));

            int result = entityManager.createQuery(command).executeUpdate();
            if (result >= 1) {
                return Optional.of(nextValue + size);
            }
            return Optional.empty();
        }));
    }

    @Override
    public void save(String segmentName, Long initValue) {
        this.transactionTemplate.execute(status -> {
            this.entityManager.persist(new InnerIdentifier(segmentName, initValue));
            this.entityManager.flush();
            return null;
        });
    }

}
