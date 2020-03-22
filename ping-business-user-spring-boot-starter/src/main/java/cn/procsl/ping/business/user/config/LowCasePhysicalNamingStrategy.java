package cn.procsl.ping.business.user.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author procsl
 * @date 2019/12/18
 */
public class LowCasePhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Value("${ping.business.table.prefix:pb_}")
    protected String tablePrefix;

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return this.castTranslate(this.tablePrefix, name);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return this.castTranslate(name);
    }

    protected Identifier castTranslate(Identifier name) {
        if (name == null) {
            return null;
        }
        return Identifier.toIdentifier(name.getText().toLowerCase());
    }

    private Identifier castTranslate(String table, Identifier name) {
        return Identifier.toIdentifier(table.concat(name.getText().toLowerCase()));
    }


}
