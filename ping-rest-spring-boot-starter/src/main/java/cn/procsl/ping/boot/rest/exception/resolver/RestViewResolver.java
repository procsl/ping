package cn.procsl.ping.boot.rest.exception.resolver;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import cn.procsl.ping.boot.rest.view.JsonView;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.ServletContext;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML;

/**
 * @author procsl
 * @date 2020/01/02
 */
@RequiredArgsConstructor
public class RestViewResolver extends ContentNegotiatingViewResolver {


    final private EnumMap<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes;
    final private InternalResourceViewResolver internalResourceViewResolver;
    HashMap<String, MediaType> cache = new HashMap<>(10);
    private View defaultView;
    private List<View> defaultViews;

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {

        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        List<MediaType> requestedMediaTypes = this.getMediaTypes(((ServletRequestAttributes) attrs).getRequest());
        if (viewName.startsWith("redirect:") || viewName.startsWith("forward:")) {
            return internalResourceViewResolver.resolveViewName(viewName, locale);
        }

        if (requestedMediaTypes == null) {
            return this.defaultView;
        }

        return this.getBestView(defaultViews, requestedMediaTypes, attrs);
    }


    @Nullable
    protected View getBestView(List<View> views, List<MediaType> requestedMediaTypes, RequestAttributes attrs) {
        for (MediaType mediaType : requestedMediaTypes) {
            for (View view : views) {

                MediaType currentContentType = getMediaType(view.getContentType());
                if (!mediaType.isCompatibleWith(currentContentType)) {
                    continue;
                }

                attrs.setAttribute(View.SELECTED_CONTENT_TYPE, mediaType, RequestAttributes.SCOPE_REQUEST);
                return view;
            }
        }
        return defaultView;
    }

    @Override
    protected void initServletContext(ServletContext servletContext) {
        super.initServletContext(servletContext);
        defaultViews = this.getDefaultViews();

        if (mediaTypes.containsKey(RestWebProperties.MetaMediaType.json)) {
            for (View view : defaultViews) {
                if (APPLICATION_JSON.isCompatibleWith(getMediaType(view.getContentType()))) {
                    this.defaultView = view;
                    return;
                }
            }
        }

        if (mediaTypes.containsKey(RestWebProperties.MetaMediaType.xml)) {
            for (View view : defaultViews) {
                if (APPLICATION_XML.isCompatibleWith(getMediaType(view.getContentType()))) {
                    this.defaultView = view;
                    return;
                }
            }
        }

        if (mediaTypes.containsKey(RestWebProperties.MetaMediaType.yaml)) {
            for (View view : defaultViews) {
                if (new MediaType("application/yaml").isCompatibleWith(getMediaType(view.getContentType()))) {
                    this.defaultView = view;
                    return;
                }
            }
        }

        if (defaultViews.isEmpty()) {
            this.defaultView = new JsonView(new JsonMapper(), RestWebProperties.modelKey);
            return;
        }

        this.defaultView = defaultViews.get(0);
    }

    private MediaType getMediaType(String contentType) {
        if (cache.containsKey(contentType)) {
            return cache.get(contentType);
        }

        MediaType mediatype = parseMediaType(contentType);
        cache.put(contentType, mediatype);
        return mediatype;
    }
}
