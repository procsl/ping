package cn.procsl.ping.boot.rest.resolver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;
import java.util.Locale;

import static org.springframework.web.servlet.view.UrlBasedViewResolver.FORWARD_URL_PREFIX;
import static org.springframework.web.servlet.view.UrlBasedViewResolver.REDIRECT_URL_PREFIX;

/**
 * @author procsl
 * @date 2020/01/02
 */
@RequiredArgsConstructor
public class RestViewResolver extends ContentNegotiatingViewResolver {

    final InternalResourceViewResolver internalResourceViewResolver;

    final List<View> views;

    final View defaultView;

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {

        boolean bool = viewName.startsWith(FORWARD_URL_PREFIX) || viewName.startsWith(REDIRECT_URL_PREFIX);
        if (bool) {
            return internalResourceViewResolver.resolveViewName(viewName, locale);
        }

        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        List<MediaType> requestedMediaTypes = this.getMediaTypes(((ServletRequestAttributes) attrs).getRequest());

        if (requestedMediaTypes == null) {
            return this.defaultView;
        }

        return this.getBestView(this.views, requestedMediaTypes, attrs);
    }


    @Nullable
    protected View getBestView(List<View> views, List<MediaType> requestedMediaTypes, RequestAttributes attrs) {
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
        return this.defaultView;
    }

}
