package cn.procsl.business.user.web;

import lombok.Setter;

import java.util.Set;

/**
 * @author procsl
 * @date 2020/02/07
 */
@Setter
public class FilterProperties {

    private FilterType type;

    private Set<String> fields;

    public enum FilterType {
        INCLUDE, EXCLUDE
    }

    public boolean matches(String path) {
        switch (this.type) {
            // 仅排除fields
            case EXCLUDE:
                for (String field : fields) {
                    if (path.startsWith(field)) {
                        return false;
                    }
                }
                return true;
            // 仅包含
            case INCLUDE:
                for (String field : fields) {
                    if (path.startsWith(field)) {
                        return true;
                    }
                }
                return false;
        }
        return false;
    }
}
