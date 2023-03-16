package cn.procsl.ping.boot.product.service;

import cn.procsl.ping.boot.product.TestProductApplication;
import cn.procsl.ping.boot.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;


@Indexed
@Service
@Validated
@Transactional
@Rollback(value = false)
@SpringBootTest(classes = TestProductApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProductServiceTest {


    @Inject
    JpaRepository<Product, Long> jpaRepository;

    @Test
    public void productCreator() {

        for (int i = 0; i < 1001; i++) {
            Product product = new Product("test");
            jpaRepository.save(product);
        }

    }


}