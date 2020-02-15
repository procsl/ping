package cn.procsl.ping.web.serializable;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * @author procsl
 * @date 2020/02/05
 */
@JsonFilter(SerializableFilter.FILTER_ID)
public class PropertyFilterMixin {
}
