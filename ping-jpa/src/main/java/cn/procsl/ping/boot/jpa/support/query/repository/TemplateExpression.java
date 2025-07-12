package cn.procsl.ping.boot.jpa.support.query.repository;

record TemplateExpression(String template, Object... args) implements WhereExpression {
    @Override
    public String toClauseString(BuilderContext context) {
        return template.formatted(args);
    }

    @Override
    public String toString() {
        return this.toClauseString(null);
    }
}
