package cn.procsl.ping.boot.jpa.domain;

import cn.procsl.ping.boot.jpa.support.extension.JpaExtensionRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Repository;

@Indexed
@Repository
public interface ExtensionRepository extends JpaExtensionRepository<TestEntity, Long> {

    int returnInt();

}
