package cn.procsl.ping.boot.jpa.domain;

import cn.procsl.ping.boot.jpa.support.extension.JpaExtensionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Repository;

import java.util.List;

@Indexed
@Repository
public interface ExtensionRepository extends JpaExtensionRepository<TestEntity, Long> {

    List<TestEntity> findAllBy();


}
