package cn.procsl.business.user.web.component;

import com.github.bohnman.squiggly.web.RequestSquigglyContextProvider;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author procsl
 * @date 2020/01/11
 */
@Slf4j
public class FieldFilterProvider extends RequestSquigglyContextProvider {

    public FieldFilterProvider(String filterParam, String defaultFilter) {
        super(filterParam, defaultFilter);
    }

    @Override
    protected boolean isFilteringEnabled(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    @Override
    protected String customizeFilter(String filter, HttpServletRequest request, Class beanClass) {
        return "**";
    }
}
