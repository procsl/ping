package cn.procsl.ping.boot.product.api;

import cn.procsl.ping.boot.web.encrypt.SecurityId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ProductDTO implements Serializable {

    @SecurityId
    Long id;

    String productName;

    public ProductDTO(Long id, String productName) {
        this.id = id;
        this.productName = productName;
    }
}
