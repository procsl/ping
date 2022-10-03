package cn.procsl.ping.boot.admin.web.user;

import cn.procsl.ping.boot.admin.domain.user.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Past;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class UserPropDTO implements Serializable {

    @NotEmpty
    @Schema(description = "用户昵称")
    String name;

    @Schema(description = "用户性别")
    Gender gender;

    @Schema(description = "身份证号")
    String cardNumber;

    @Schema(description = "民族")
    String nation;

    @Schema(description = "生日")
    @Past
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    Date birthday;

    @Schema(description = "毕业院校")
    String graduateSchool;

    @Schema(description = "电话")
    String telephone;

    @Schema(description = "邮箱")
    @Email String email;


    @Schema(description = "用户备注")
    String remark;

    @Builder
    public UserPropDTO(String name, Gender gender, String cardNumber, String nation, Date birthday,
                       String graduateSchool, String telephone, String email, String remark) {
        this.name = name;
        this.gender = gender;
        this.cardNumber = cardNumber;
        this.nation = nation;
        this.birthday = birthday;
        this.graduateSchool = graduateSchool;
        this.telephone = telephone;
        this.email = email;
        this.remark = remark;
    }
}
