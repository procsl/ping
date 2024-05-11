package cn.procsl.ping.boot.system.domain.ac;

import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

/**
 * 访问资源描述
 */
public interface Resource {

    /**
     * 资源主体目标
     */
    @NotBlank
    String getObject();

    /**
     * 资源描述
     */
    Optional<String> getDescription();

}
