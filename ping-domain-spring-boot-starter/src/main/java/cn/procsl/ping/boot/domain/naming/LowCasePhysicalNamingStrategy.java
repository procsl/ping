package cn.procsl.ping.boot.domain.naming;

import cn.procsl.ping.boot.domain.config.DomainProperties;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * @author procsl
 * @date 2019/12/18
 */
@RequiredArgsConstructor
public class LowCasePhysicalNamingStrategy implements PhysicalNamingStrategy {

    final DomainProperties dataProperties;

    private Converter<String, String> converter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

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
        if (name == null) {
            return null;
        }
        String prefix = this.dataProperties.getTablePrefix().concat(this.dataProperties.getDot());

        // 像qq 这种会有问题
        String conStr = prefix + converter.convert(name.getText());
        do {
            conStr = conStr.replace("__", "_");
        } while (conStr.contains("__"));
        return Identifier.toIdentifier(conStr);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name == null) {
            return null;
        }

        String conStr = converter.convert(name.getText());
        do {
            conStr = conStr.replace("__", "_");
        } while (conStr.contains("__"));
        return Identifier.toIdentifier(conStr);
    }

}
