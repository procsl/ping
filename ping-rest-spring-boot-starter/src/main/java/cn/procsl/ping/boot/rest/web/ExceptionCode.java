package cn.procsl.ping.boot.rest.web;

import cn.procsl.ping.boot.rest.annotation.SkipFilter;
import lombok.*;

import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/02/19
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@SkipFilter
public class ExceptionCode implements Serializable {

    private String code;

    private String message;

}
