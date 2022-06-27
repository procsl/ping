package cn.procsl.ping.boot.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@RequiredArgsConstructor
@DisplayName("商品服务单元测试")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ProducerApplication.class})
public class ProductServiceTest {

    @Autowired
    ProductService productService;

    @Autowired
    JpaRepository<Product, Long> jpaRepository;

    @Test
    public
    @DisplayName("商品发布测试")
    void publish() {
        productService.publish(1L);
    }
}
