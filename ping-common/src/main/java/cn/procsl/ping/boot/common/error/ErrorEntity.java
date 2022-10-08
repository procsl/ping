package cn.procsl.ping.boot.common.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;

public interface ErrorEntity {

    @JsonIgnore
    @Schema(hidden = true)
    @NonNull
    default Integer getHttpStatus() {
        return 501;
    }

    String getCode();

    String getMessage();

}
