package cn.procsl.ping.boot.product.api;

import cn.procsl.ping.boot.web.encrypt.DecryptDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Slf4j
public class ProductController {

    @PostMapping("/v1/products/{id}")
    public void publishProduct(@PathVariable DecryptDTO id) {
        log.debug("发布商品");
    }

    @PostMapping("/v1/products")
    public void createProduct(@RequestBody ProductDTO productDTO) {
        log.debug("创建商品");
    }

    @GetMapping("/v1/products")
    public ProductDTO findProduct(@RequestParam(required = false) DecryptDTO id) {
        return new ProductDTO(id == null ? 1234567L : id.getId(), "你好啊");
    }

}
