package cn.procsl.ping.boot.jpa.support.query.repository;

import java.util.List;

public interface FromObject extends Clause {


    String baseFormClause();

    String alias();

    List<JoinObject> joins();

}
