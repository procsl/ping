package cn.procsl.ping.boot.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    final JpaRepository<Product, Long> jpaRepository;

    @Transactional
    public void publish(Long productId) {
        jpaRepository.getById(productId);
    }

}
