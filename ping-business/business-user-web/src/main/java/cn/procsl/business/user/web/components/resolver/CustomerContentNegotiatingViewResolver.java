package cn.procsl.business.user.web.components.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.SmartView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author procsl
 * @date 2019/12/28
 */
public class CustomerContentNegotiatingViewResolver extends ContentNegotiatingViewResolver {

    @Autowired
    HttpServletRequest httpServletRequest;

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        Assert.state(attrs instanceof ServletRequestAttributes, "No current ServletRequestAttributes");
        List<MediaType> requestedMediaTypes = getMediaTypes(((ServletRequestAttributes) attrs).getRequest());
        if (requestedMediaTypes != null) {
            List<View> candidateViews = getCandidateViews(viewName, locale, requestedMediaTypes);
            View bestView = getBestView(candidateViews, requestedMediaTypes, attrs);
            if (bestView != null) {
                httpServletRequest.setAttribute("mark_view", bestView);
                return bestView;
            }
        }

        return this.getDefaultViews().get(0);
    }

    private List<View> getCandidateViews(String viewName, Locale locale, List<MediaType> requestedMediaTypes)
            throws Exception {

        List<View> candidateViews = new ArrayList<>();
        for (ViewResolver viewResolver : this.getViewResolvers()) {
            View view = viewResolver.resolveViewName(viewName, locale);
            if (view != null) {
                candidateViews.add(view);
            }
            for (MediaType requestedMediaType : requestedMediaTypes) {
                List<String> extensions = this.getContentNegotiationManager().resolveFileExtensions(requestedMediaType);
                for (String extension : extensions) {
                    String viewNameWithExtension = viewName + '.' + extension;
                    view = viewResolver.resolveViewName(viewNameWithExtension, locale);
                    if (view != null) {
                        candidateViews.add(view);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(this.getDefaultViews())) {
            candidateViews.addAll(this.getDefaultViews());
        }
        return candidateViews;
    }


    private View getBestView(List<View> candidateViews, List<MediaType> requestedMediaTypes, RequestAttributes attrs) {
        for (View candidateView : candidateViews) {
            if (candidateView instanceof SmartView) {
                SmartView smartView = (SmartView) candidateView;
                if (smartView.isRedirectView()) {
                    return candidateView;
                }
            }
        }
        for (MediaType mediaType : requestedMediaTypes) {
            for (View candidateView : candidateViews) {
                if (StringUtils.hasText(candidateView.getContentType())) {
                    MediaType candidateContentType = MediaType.parseMediaType(candidateView.getContentType());
                    if (mediaType.isCompatibleWith(candidateContentType)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Selected '" + mediaType + "' given " + requestedMediaTypes);
                        }
                        attrs.setAttribute(View.SELECTED_CONTENT_TYPE, mediaType, RequestAttributes.SCOPE_REQUEST);
                        return candidateView;
                    }
                }
            }
        }
        return null;
    }

}
