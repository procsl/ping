package cn.procsl.business.user.web.component.resolver;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import java.util.*;

/**
 * @author procsl
 * @date 2020/01/02
 */
public class RestViewResolver extends ContentNegotiatingViewResolver {

    @Autowired
    protected Set<View> views;

    @Setter
    protected View defaultView;

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {

        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        List<MediaType> requestedMediaTypes = this.getMediaTypes(((ServletRequestAttributes) attrs).getRequest());

        if (requestedMediaTypes == null) {
            return defaultView;
        }

        View bestView = this.getBestView(this.views, requestedMediaTypes, attrs);
        if (bestView != null) {
            return bestView;
        }

        // TODO
        return defaultView;
    }


    @Nullable
    protected View getBestView(Set<View> views, List<MediaType> requestedMediaTypes, RequestAttributes attrs) {
        for (MediaType mediaType : requestedMediaTypes) {
            for (View view : views) {
                if (!StringUtils.hasText(view.getContentType())) {
                    continue;
                }

                MediaType currentContentType = MediaType.parseMediaType(view.getContentType());
                if (!mediaType.isCompatibleWith(currentContentType)) {
                    continue;
                }

                attrs.setAttribute(View.SELECTED_CONTENT_TYPE, mediaType, RequestAttributes.SCOPE_REQUEST);
                return view;
            }
        }

        return null;
    }

}
