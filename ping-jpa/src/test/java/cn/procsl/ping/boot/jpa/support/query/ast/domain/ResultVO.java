package cn.procsl.ping.boot.jpa.support.query.ast.domain;

import cn.procsl.ping.boot.jpa.support.query.Projection;
import lombok.Data;

import java.io.Serializable;

@Data
@Projection(entity = Teacher.class, alias = "teacher")
public class ResultDTO implements Serializable {

    String id;

    String name;
}
