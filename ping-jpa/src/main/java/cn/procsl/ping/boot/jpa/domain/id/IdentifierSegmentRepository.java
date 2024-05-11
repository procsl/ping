package cn.procsl.ping.boot.jpa.support;

import java.util.Optional;

public interface IdentifierSegmentRepository {

    Optional<Long> incrementBy(String segmentName, int size);

    void save(String segmentName, Long id);

}
