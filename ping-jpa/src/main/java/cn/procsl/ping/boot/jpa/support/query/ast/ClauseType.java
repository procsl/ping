package cn.procsl.ping.boot.jpa.support.query.ast;

public enum ClauseType {

    select,
    from,
    where,
    order_by,
    group_by,
    having,
    // sql注释, 会追加在sql之前
    annotation,
    // 不参与生成sql
    none;

}
