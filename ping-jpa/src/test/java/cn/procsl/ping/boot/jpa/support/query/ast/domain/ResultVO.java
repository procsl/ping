package cn.procsl.ping.boot.jpa.support.query.ast.domain;

import cn.procsl.ping.boot.jpa.support.query.ast.ViewConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ResultVO implements Serializable {

    @ViewConstructor()
    public ResultVO(Long id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    Long id;

    String name;

    String desc;
}
