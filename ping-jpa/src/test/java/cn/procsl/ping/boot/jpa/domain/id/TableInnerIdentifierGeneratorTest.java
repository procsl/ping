//package cn.procsl.ping.boot.jpa.domain.id;
//
//import cn.procsl.ping.boot.jpa.TestJpaApplication;
//import jakarta.inject.Inject;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.RepeatedTest;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//
//import java.util.concurrent.*;
//
//@Slf4j
//@Rollback
//@SpringBootTest(classes = TestJpaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
//public class TableInnerIdentifierGeneratorTest {
//
//    @Autowired
//    UniqueService uniqueService;
//
//
//
//    @RepeatedTest(100)
//    public void nextId() {
//        uniqueService.saveService();
//    }
//
//    @RepeatedTest(100)
//    public void multiThread() throws ExecutionException, InterruptedException {
//        ThreadPoolExecutor executor = new ThreadPoolExecutor(30, 100, 10,
//                TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>(),
//                Executors.defaultThreadFactory(),
//                new ThreadPoolExecutor.AbortPolicy()
//        );
//
//        // 开启100次任务
//        for (int i = 0; i < 100; i++) {
//            CompletableFuture<Throwable> result = CompletableFuture.supplyAsync(() -> {
//                uniqueService.saveService();
//                return (Throwable) null;
//            }, executor).whenComplete((r, e) -> log.warn("多线程测试: {}", r, e)).exceptionally(throwable -> throwable);
//            Throwable error = result.get();
//            if (error != null) {
//                throw new RuntimeException(error);
//            }
//        }
//    }
//
//    @Test
//    public void uniqueService() {
//        this.uniqueService.test(100);
//    }
//}
