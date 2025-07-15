package cn.procsl.ping.boot.jpa.support.query.repository;

record TemplateOperator(String template, Object... args) implements Operator {
    @Override
    public String toClauseString(BuilderContext context) {
        if (args != null && args.length > 0) {
            return template.formatted(args);
        }
        return template;
    }

    @Override
    public String toString() {
        return this.toClauseString(null);
    }
}
