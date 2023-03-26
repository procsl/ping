package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.common.dto.ID;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "ID", implementation = String.class, example = "Q0GgEiBbxEB4kCNHheTNb", minLength = 22, maxLength = 22)
public class DecryptDTO implements ID<Long> {

    Long id;


    public DecryptDTO(Long id) {
        this.id = id;
    }
}
