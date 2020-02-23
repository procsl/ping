package cn.procsl.ping.boot.rest.web;

import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.UrlPathHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/02/23
 */
public class RestPathExtensionContentNegotiationStrategy extends PathExtensionContentNegotiationStrategy {

    private Map<String, List<MediaType>> supportMediaTypes;

    public RestPathExtensionContentNegotiationStrategy(PathExtensionContentNegotiationStrategy strategy) {
        super(strategy.getMediaTypes());
        this.setIgnoreUnknownExtensions(strategy.isIgnoreUnknownExtensions());
        this.setUseJaf(!strategy.isUseRegisteredExtensionsOnly());
        this.setUseRegisteredExtensionsOnly(strategy.isUseRegisteredExtensionsOnly());
        UrlPathHelper path = findUrlPathHelper(strategy);
        this.setUrlPathHelper(path);
        supportMediaTypes = new HashMap<>(strategy.getMediaTypes().size());
        strategy.getMediaTypes().forEach(this::supportMediaType);
    }

    public RestPathExtensionContentNegotiationStrategy supportMediaType(String key, MediaType mediaType) {
        if (this.supportMediaTypes.containsKey(key)) {
            this.supportMediaTypes.get(key).add(mediaType);
        } else {
            LinkedList<MediaType> list = new LinkedList<>();
            list.push(mediaType);
            this.supportMediaTypes.put(key, list);
        }
        return this;
    }

    /**
     * fuck spring
     *
     * @param instance
     * @return
     */
    public UrlPathHelper findUrlPathHelper(PathExtensionContentNegotiationStrategy instance) {
        Field field = ReflectionUtils.findField(instance.getClass(), "urlPathHelper", UrlPathHelper.class);
        field.setAccessible(true);
        Object obj = ReflectionUtils.getField(field, instance);
        field.setAccessible(false);
        if (obj != null) {
            return (UrlPathHelper) obj;
        }
        return null;
    }

    @Override
    public List<MediaType> resolveMediaTypeKey(NativeWebRequest webRequest, String key) throws HttpMediaTypeNotAcceptableException {
        return supportMediaTypes.getOrDefault(key, MEDIA_TYPE_ALL_LIST);
    }

}
