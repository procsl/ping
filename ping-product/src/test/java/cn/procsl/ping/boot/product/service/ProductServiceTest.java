package cn.procsl.ping.boot.product.service;

import cn.procsl.ping.boot.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;


@Indexed
@Service
@Validated
@Transactional
@RequiredArgsConstructor
@Rollback(value = false)
public class ProductServiceTest {


    final JpaRepository<Product, Long> jpaRepository;

    @Test
    public void productCreator() {

        Product product = new Product("test");

        for (int i = 0; i < 100; i++) {
            jpaRepository.save(product);
        }

    }


}