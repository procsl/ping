package cn.procsl.ping.boot.domain.naming;

import cn.procsl.ping.boot.domain.config.DomainProperties;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author procsl
 * @date 2019/12/18
 */
@RequiredArgsConstructor
@Slf4j
public class LowCasePhysicalNamingStrategy implements PhysicalNamingStrategy {

    final DomainProperties dataProperties;

    private final Converter<String, String> converter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

    private final static Set<String> WHITELIST = new HashSet<>();

    {
        WHITELIST.add("DTYPE");
        WHITELIST.add("ID");
        WHITELIST.add("id");
    }

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

        if (WHITELIST.contains(name.getText())) {
            return name;
        }

        String tmp = name.getText();
        Map<String, String> prefixs = this.dataProperties.getModulePrefix();
        for (String key : prefixs.keySet()) {
            String fmt = String.format("_$%s:", key);
            tmp = tmp.replace(fmt, "_");

            String fmtFirst = String.format("$%s:", key);
            String vle = prefixs.get(key);
            if (vle == null || vle.isEmpty()) {
                vle = "";
            } else {
                vle = vle.concat(this.dataProperties.getDot());
            }
            tmp = tmp.replace(fmtFirst, vle);
        }

        String conStr = convertTo(tmp);
        log.debug("Entity:{} mapping table{}", name.getText(), conStr);
        return Identifier.toIdentifier(conStr);
    }

    private String getName(boolean isModel, String text) {
        if (!isModel) {
            return text;
        }

        Set<String> keys = dataProperties.getModulePrefix().keySet();
        for (String s : keys) {
            text = text.replaceAll(s + "\\.", "");
        }
        return text;
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

        if (WHITELIST.contains(name.getText())) {
            return name;
        }

        String tmp = name.getText();
        Map<String, String> prefixs = this.dataProperties.getModulePrefix();
        for (String key : prefixs.keySet()) {
            String fmt = String.format("$%s:", key);
            tmp = tmp.replace(fmt, "");
        }

        String conStr = convertTo(tmp);
        return Identifier.toIdentifier(conStr);
    }

    public String convertTo(String text) {
        String conStr = converter.convert(text);
        do {
            conStr = conStr.replace("__", "_");
        } while (conStr.contains("__"));
        return conStr;
    }

}
