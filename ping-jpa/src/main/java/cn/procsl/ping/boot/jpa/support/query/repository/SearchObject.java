package cn.procsl.ping.boot.jpa.support.query.repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public interface SearchObject extends Clause {
    // 查询字段描述对象
    //  [查询语句]  as [别名]
    List<SelectObject> select();

    // 查询对象描述
    // [查询对象] as [别名]
    // 查询对象可以是查询语句,也可以是表名
    List<FromObject> from();

    // 条件描述对象
    // [条件语句] 条件语句以 AND 连接
    // 条件语句可以有基本语句和条件语句嵌套构成
    //  [条件语句参数占位符]
    WhereObject where();

    // 分组描述对象
    // 分组对象由 查询字段中的别名组成
    // [分组语句]有先后顺序

    // 排序描述对象
    //


    // 排序描述对象
    default List<SortObject> sort() {
        return Collections.emptyList();
    }

    @Override
    default String toClauseString(BuilderContext context) {

        Collector<CharSequence, ?, String> a;
        String item;
        String newLine;
        if (context.isFormat()) {
            item = "\n\t";
            newLine = "\n";
        } else {
            item = " ";
            newLine = " ";
        }
        a = Collectors.joining("," + item);
        String select = this.select().stream().map(s -> s.toClauseString(context)).collect(a);
        String from = this.from().stream().map(f -> f.toClauseString(context)).collect(a);
        String main = "%sselect%s%s%sfrom%s%s".formatted(newLine, item, select, newLine, item, from);
        String where = this.where().getOperator().toClauseString(context);

        if (where == null) {
            where = "";
        }

        String i = where.replaceAll("\\s+", "").trim();
        if (i.isEmpty()) {
            where = "";
        }

        if (!where.isEmpty()) {
            main = "%s%swhere%s%s".formatted(main, newLine, item, where);
        }

        String sorts = this.sort().stream().map(s -> s.toClauseString(context)).collect(Collectors.joining(", "));
        if (!sorts.isEmpty()) {
            main = "%s%sorder by%s%s".formatted(main, newLine, item, sorts);
        }
        return main;
    }
}
