package cn.procsl.ping.boot.product.service;

import cn.procsl.ping.boot.product.TestProductApplication;
import cn.procsl.ping.boot.product.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;


//@Component
//@Validated
//@Transactional
//@Rollback(value = false)
@SpringBootTest(classes = TestProductApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProductServiceTest {


    //    @Autowired
    @Autowired
    JpaRepository<Product, Long> jpaRepository;

    //    @Autowired
//    ProductRepository jpaRepository;

    @Test
    public void productCreator() {

        Product product = new Product("test");

        for (int i = 0; i < 100; i++) {
            jpaRepository.save(product);
        }

    }


}
