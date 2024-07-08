package cn.procsl.ping.boot.jpa.support.query.builder.parse;

import java.lang.reflect.Executable;

class GetterSelectClause extends StringSelectClause {

    final Executable executable;

    public GetterSelectClause(int index, String name, Executable executable) {
        super(index, name, executable.getDeclaringClass());
        this.executable = executable;
    }
}
