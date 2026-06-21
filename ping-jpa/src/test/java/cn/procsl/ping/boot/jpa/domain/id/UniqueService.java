//package cn.procsl.ping.boot.jpa.domain.id;
//
//import cn.procsl.ping.boot.common.utils.IdentifierGenerator;
//import cn.procsl.ping.boot.common.utils.SimpleLongIdGenerator;
//import jakarta.inject.Inject;
//import jakarta.persistence.EntityManager;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Slf4j
//@Service
//public class UniqueService {
//
//    IdentifierGenerator<Long> idGenerator = new SimpleLongIdGenerator();
//
//    @Autowired
//    EntityManager entityManager;
//
//    @Transactional
//    public void saveService() {
//        for (int i = 0; i < 50; i++) {
//            Long a = idGenerator.nextId();
//            log.info("nextId: {}", a);
//            entityManager.persist(new Unique(a, "test"));
//        }
//        entityManager.flush();
//    }
//
//    public void test(int times) {
//        for (int i = 0; i < times; i++) {
//            idGenerator.nextId();
//        }
//    }
//
//
//}
