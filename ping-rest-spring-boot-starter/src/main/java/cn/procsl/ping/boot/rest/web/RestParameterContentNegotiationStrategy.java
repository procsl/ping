package cn.procsl.ping.boot.rest.web;

import cn.procsl.ping.boot.rest.exception.RestHttpMediaTypeNotAcceptableException;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author procsl
 * @date 2020/03/07
 */
public class RestParameterContentNegotiationStrategy implements ContentNegotiationStrategy {

    private Map<String, List<MediaType>> mediaTypes;

    private List<MediaType> all = Collections.singletonList(MediaType.ALL);

    @Setter
    private boolean useRegisteredExtensionsOnly = false;

    @Setter
    private boolean ignoreUnknownExtensions = false;

    @Setter
    private String parameterName = "content";

    public RestParameterContentNegotiationStrategy(Map<String, List<MediaType>> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    @Nullable
    protected String getMediaTypeKey(NativeWebRequest request) {
        return request.getParameter(parameterName);
    }

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest webRequest) throws HttpMediaTypeNotAcceptableException {

        String key = getMediaTypeKey(webRequest);
        if (isEmpty(key)) {
            return all;
        }
        List<MediaType> type = this.mediaTypes.get(key);
        if (!CollectionUtils.isEmpty(type)) {
            return type;
        }
        if (this.ignoreUnknownExtensions) {
            return all;
        }
        if (this.useRegisteredExtensionsOnly) {
            throw new RestHttpMediaTypeNotAcceptableException("不支持的响应类型:" + key);
        }
        return all;
    }
}
