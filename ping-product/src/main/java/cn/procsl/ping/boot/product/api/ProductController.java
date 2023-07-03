package cn.procsl.ping.boot.product.api;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import cn.procsl.ping.boot.web.annotation.RequestBodySecurityIds;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProductController {

    final HttpServletRequest request;

    @PostMapping("/v1/products/{id}")
    public void publishProduct(@PathVariable(name = "id") @SecurityId Long product) {
        log.debug("发布商品: {}", product);
    }

    @PostMapping("/v1/products")
    public void createProduct(@RequestBody ProductDTO productDTO) {
        log.debug("创建商品: {}", productDTO);
    }

    @PutMapping("/v1/products")
    @RequestBodySecurityIds
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createProduct2(@RequestBody Collection<Long> ids) {
        log.debug("创建商品: {}", ids);
    }

    @PatchMapping("/v1/products")
    public void createProduct3(@RequestBody List<String> productDTO) {
        log.debug("创建商品: {}", productDTO);
    }

    @GetMapping("/v1/products")
    public ProductDTO findProduct(@RequestParam(required = false)  @SecurityId Long id) {
        return new ProductDTO(id, "你好啊");
    }


}
