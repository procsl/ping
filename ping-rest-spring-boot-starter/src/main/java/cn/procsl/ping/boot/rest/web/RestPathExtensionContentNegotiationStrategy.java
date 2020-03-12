package cn.procsl.ping.boot.rest.web;

import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;

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

    public RestPathExtensionContentNegotiationStrategy(Map<String, List<MediaType>> supportMediaTypes) {
        this.supportMediaTypes = new HashMap<>();
        this.supportMediaTypes.putAll(supportMediaTypes);
    }

    public RestPathExtensionContentNegotiationStrategy() {
    }

    public RestPathExtensionContentNegotiationStrategy supportMediaType(String key, MediaType mediaType) {
        if (key == null || mediaType == null) {
            return this;
        }
        if (this.supportMediaTypes.containsKey(key)) {
            this.supportMediaTypes.get(key).add(mediaType);
        } else {
            LinkedList<MediaType> list = new LinkedList<>();
            list.push(mediaType);
            this.supportMediaTypes.put(key, list);
        }
        return this;
    }

    @Override
    public List<MediaType> resolveMediaTypeKey(NativeWebRequest webRequest, String key) throws HttpMediaTypeNotAcceptableException {
        return supportMediaTypes.getOrDefault(key, MEDIA_TYPE_ALL_LIST);
    }

}
