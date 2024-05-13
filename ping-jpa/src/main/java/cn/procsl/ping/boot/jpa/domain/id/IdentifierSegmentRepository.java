package cn.procsl.ping.boot.jpa.domain.id;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

import java.util.Optional;

@NoRepositoryBean
public interface IdentifierSegmentRepository {

    @NonNull
    Optional<Long> incrementBy(String segmentName, int size);

    void save(String segmentName, Long idValue);

}
