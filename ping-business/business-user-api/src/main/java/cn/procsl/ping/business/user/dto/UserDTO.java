package cn.procsl.ping.business.user.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户对象
 *
 * @author procsl
 * @date 2019/12/11
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {

    /**
     * 账户姓名
     */
    @NotEmpty
    @Size(max = 20)
    protected String name;


}
