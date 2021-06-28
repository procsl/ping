package cn.procsl.ping.boot.rest.web;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.ParameterContentNegotiationStrategy;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.accept.ServletPathExtensionContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.util.StringUtils.isEmpty;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

/**
 * @author procsl
 * @date 2020/02/23
 */
public class RestExtensionContentNegotiationStrategy implements ContentNegotiationStrategy {

    private final EnumMap<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes;

    private final String content = "format";
    List<ContentNegotiationStrategy> strategies = new LinkedList();
    @Setter
    private ServletContext servletContext;
    @Setter
    private boolean enableCustom = false;
    private List<MediaType> defaultMedias;

    public RestExtensionContentNegotiationStrategy(EnumMap<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest webRequest) throws HttpMediaTypeNotAcceptableException {

        Object cache = webRequest.getAttribute(this.getClass().getName(), SCOPE_REQUEST);
        if (cache != null) {
            return (List<MediaType>) cache;
        }

        List<MediaType> types = MEDIA_TYPE_ALL_LIST;
        for (ContentNegotiationStrategy strategy : this.strategies) {
            types = strategy.resolveMediaTypes(webRequest);
            boolean bool = types.equals(MEDIA_TYPE_ALL_LIST);
            if (bool) {
                continue;
            }
            if (enableCustom) {
                break;
            }
            webRequest.setAttribute(this.getClass().getName(), types, SCOPE_REQUEST);
            return types;
        }

        RestWebProperties.MetaMediaType key = this.findKey(webRequest);
        if (key == null) {
            webRequest.setAttribute(this.getClass().getName(), types, SCOPE_REQUEST);
            return types;
        }

        List<MediaType> tmp = this.mediaTypes.get(key);
        if (tmp == null) {
            webRequest.setAttribute(this.getClass().getName(), types, SCOPE_REQUEST);
            return types;
        }

        LinkedList<MediaType> custom = new LinkedList<>(types);
        custom.addAll(tmp);
        webRequest.setAttribute(this.getClass().getName(), types, SCOPE_REQUEST);
        return custom;
    }

    private RestWebProperties.MetaMediaType findKey(NativeWebRequest webRequest) {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }

        String url = request.getRequestURI();
        if (url.endsWith(".json")) {
            return json;
        }

        if (url.endsWith(".xml")) {
            return xml;
        }

        if (url.endsWith(".yaml") || url.endsWith(".yml")) {
            return yaml;
        }

        String params = webRequest.getParameter(this.content);
        if (isEmpty(params)) {
            webRequest.getContextPath();
        }

        try {
            return RestWebProperties.MetaMediaType.valueOf(params);
        } catch (Exception e) {
            return null;
        }
    }

    public void afterPropertiesSet() {

        Map<String, MediaType> mediaType = new HashMap<>(3);
        mediaType.put("json", APPLICATION_JSON);
        mediaType.put("xml", MediaType.APPLICATION_XML);
        mediaType.put("yaml", new MediaType("application", "yaml"));
        {
            PathExtensionContentNegotiationStrategy strategy;
            strategy = new ServletPathExtensionContentNegotiationStrategy(this.servletContext, mediaType);
            strategy.setIgnoreUnknownExtensions(false);
            strategy.setUseRegisteredExtensionsOnly(true);
            strategies.add(strategy);
        }

        {
            ParameterContentNegotiationStrategy strategy = new ParameterContentNegotiationStrategy(mediaType);
            strategy.setParameterName(this.content);
            strategy.setUseRegisteredExtensionsOnly(true);
            strategy.setIgnoreUnknownExtensions(false);
            strategies.add(strategy);
        }
        defaultMedias = Collections.singletonList(APPLICATION_JSON);

        if (mediaTypes.containsKey(yaml)) {
            MediaType defaultMedia = new MediaType("application", "yaml");
            List<MediaType> yamlTypes = mediaTypes.get(json);
            if ((!yamlTypes.contains(defaultMedia)) || yamlTypes.size() > 1) {
                enableCustom = true;
            }
        }

        if (mediaTypes.containsKey(xml)) {
            List<MediaType> xmlTypes = mediaTypes.get(xml);
            this.defaultMedias = xmlTypes;
            if ((!xmlTypes.contains(MediaType.APPLICATION_XML)) || xmlTypes.size() > 1) {
                enableCustom = true;
            }
        }

        if (mediaTypes.containsKey(json)) {
            List<MediaType> jsonTypes = mediaTypes.get(json);
            this.defaultMedias = jsonTypes;
            if ((!jsonTypes.contains(APPLICATION_JSON)) || jsonTypes.size() > 1) {
                enableCustom = true;
            }
        }


    }
}
