package cn.procsl.ping.boot.jpa.jpql;

import cn.procsl.ping.boot.jpa.domain.TestEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpqlRepository extends org.springframework.data.repository.Repository<TestEntity, Long> {


    @Query("select s.name, s.id from TestEntity as s where s.id=?1 and s.name=?2")
    @Lock(LockModeType.READ)
    TestEntity query(Long id, String name);

}
