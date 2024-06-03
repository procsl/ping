package cn.procsl.ping.boot.jpa.domain.id;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface IdentifierSegmentRepository {

    Optional<Long> incrementBy(String segmentName, int size) throws IdentifierException;

    void save(String segmentName, Long initValue) throws IdentifierException;

}
