package cn.procsl.ping.boot.product.api;

import cn.procsl.ping.boot.product.domain.Product;
import cn.procsl.ping.boot.web.encrypt.DecryptId;
import cn.procsl.ping.boot.web.encrypt.SecurityId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Slf4j
public class ProductController {

    @PostMapping("/v1/products/{id}")
    public void publishProduct(@PathVariable(name = "id") Product product) {
        log.debug("发布商品: {}", product);
    }

    @PostMapping("/v1/products")
    public void createProduct(@RequestBody ProductDTO productDTO) {
        log.debug("创建商品: {}", productDTO);
    }

    @GetMapping("/v1/products")
    public ProductDTO findProduct(@RequestParam(required = false) @SecurityId Long id) {
        return new ProductDTO(id, "你好啊");
    }

}
