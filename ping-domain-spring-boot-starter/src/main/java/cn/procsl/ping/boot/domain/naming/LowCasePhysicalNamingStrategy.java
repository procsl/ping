package cn.procsl.ping.boot.domain.naming;

import cn.procsl.ping.boot.domain.config.DomainProperties;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.procsl.ping.boot.domain.business.utils.StringUtils.isEmpty;

/**
 * @author procsl
 * @date 2019/12/18
 */
@RequiredArgsConstructor
@Slf4j
public class LowCasePhysicalNamingStrategy implements PhysicalNamingStrategy {

    final DomainProperties dataProperties;

    private final Converter<String, String> converter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

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
        boolean isModel = isModelPattern(name.getText());
        String text = replace(isModel, name.getText());
        String newName = getName(isModel, text);
        String prefix = getPrefix(isModel, text, newName);

        String conStr = convertTo(prefix, newName);
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

    private String getPrefix(boolean isModel, String text, String name) {
        String prefix = this.dataProperties.getTablePrefix();

        if (!isModel) {
            return isEmpty(prefix) ? "" : prefix;
        }

        String[] nameSeg = name.split("_");

        for (int i = 0; i < nameSeg.length - 1; i++) {
            String tmp = nameSeg[i] + "_";
            text = text.replaceAll(tmp, "");
        }
        text = text.replaceAll(nameSeg[nameSeg.length - 1], "");

        Map<String, Long> counts = Arrays.stream(text.split("\\."))
            .collect(Collectors.groupingBy(i -> i, Collectors.counting()));

        long max = counts.values().stream().mapToLong(item -> item).max().orElse(0L);
        if (max == 0L) {
            return "";
        }

        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            if (entry.getValue().equals(max)) {
                String key = entry.getKey();
                Map<String, String> map = this.dataProperties.getModulePrefix();
                prefix = map.getOrDefault(key, prefix);
                continue;
            }
        }

        return isEmpty(prefix) ? "" : prefix;
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
        boolean isModel = isModelPattern(name.getText());
        String text = replace(isModel, name.getText());
        String newName = getName(isModel, text);
        String conStr = convertTo("", newName);
        return Identifier.toIdentifier(conStr);
    }

    public String replace(boolean isModel, String text) {
        if (isModel) {
            text = text.replaceAll("\\$|\\{|\\}", "");
        }
        return text;
    }

    public boolean isModelPattern(String name) {
        return name.contains("$") || name.contains("{") || name.contains("}");
    }

    public String convertTo(String prefix, String text) {

        if (!prefix.isEmpty()) {
            prefix = prefix.concat(this.dataProperties.getDot());
        }

        String conStr = converter.convert(prefix.concat(text));
        do {
            conStr = conStr.replace("__", "_");
        } while (conStr.contains("__"));
        return conStr;
    }

}
