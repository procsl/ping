package cn.procsl.ping.boot.jpa.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Slf4j
@Repository
class IdentifierRepositoryImpl implements IdentifierSegmentRepository {

    @Override
    public Optional<Long> incrementBy(String segmentName, int size) {
        log.info("Incrementing by {}", segmentName);
        return Optional.empty();
    }

    @Override
    public void save(String segmentName, Long id) {
        log.info("save: {}", segmentName);
    }

}
