package cn.procsl.business.user.web.component;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * @author procsl
 * @date 2020/02/05
 */
@JsonFilter(FieldsPropertyFilter.FILTER_ID)
public class PropertyFilterMixin {
}
