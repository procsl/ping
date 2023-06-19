package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.common.dto.ID;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Schema(description = "ID", implementation = String.class, example = "Q0GgEiBbxEB4kCNHheTNb", minLength = 22, maxLength = 22)
public interface DecryptId<T extends Serializable> extends ID<T> {

    @Getter
    @RequiredArgsConstructor
    class LongId implements DecryptId<Long> {
        final Long id;
    }

}
