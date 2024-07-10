package cn.procsl.ping.boot.jpa.support.query.builder.parser;

import java.lang.reflect.Executable;

final class GetterSelectClause extends StringSelectClause {

    final Executable executable;

    public GetterSelectClause(int index, String name, Executable executable) {
        super(index, name, executable.getDeclaringClass());
        this.executable = executable;
    }
}
