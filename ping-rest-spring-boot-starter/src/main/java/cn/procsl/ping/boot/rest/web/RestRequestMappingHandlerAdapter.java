/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.procsl.ping.boot.rest.web;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import cn.procsl.ping.boot.rest.handler.RestHandler;
import cn.procsl.ping.boot.rest.handler.SimpleTypeHandler;
import cn.procsl.ping.boot.rest.utils.SpringWrapperUtil;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.ui.ModelMap;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.*;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.*;
import org.springframework.web.method.support.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.annotation.ModelAndViewResolver;
import org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter;
import org.springframework.web.servlet.mvc.method.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension of {@link AbstractHandlerMethodAdapter} that supports
 * {@link RequestMapping @RequestMapping} annotated {@link HandlerMethod HandlerMethods}.
 *
 * <p>Support for custom argument and return value types can be added via
 * {@link #setCustomArgumentResolvers} and {@link #setCustomReturnValueHandlers},
 * or alternatively, to re-configure all argument and return value types,
 * use {@link #setArgumentResolvers} and {@link #setReturnValueHandlers}.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @see HandlerMethodArgumentResolver
 * @see HandlerMethodReturnValueHandler
 * @since 3.1
 */
public class RestRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

    /**
     * MethodFilter that matches {@link InitBinder @InitBinder} methods.
     */
    public static final MethodFilter INIT_BINDER_METHODS = method ->
            AnnotatedElementUtils.hasAnnotation(method, InitBinder.class);

    /**
     * MethodFilter that matches {@link ModelAttribute @ModelAttribute} methods.
     */
    public static final MethodFilter MODEL_ATTRIBUTE_METHODS = method ->
            (!AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class) &&
                    AnnotatedElementUtils.hasAnnotation(method, ModelAttribute.class));


    @Nullable
    protected List<HandlerMethodArgumentResolver> customArgumentResolvers;

    @Nullable
    protected HandlerMethodArgumentResolverComposite argumentResolvers;

    @Nullable
    protected HandlerMethodArgumentResolverComposite initBinderArgumentResolvers;

    @Nullable
    protected List<HandlerMethodReturnValueHandler> customReturnValueHandlers;

    @Nullable
    protected HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    @Nullable
    protected List<ModelAndViewResolver> modelAndViewResolvers;

    protected ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    protected List<HttpMessageConverter<?>> messageConverters;

    protected List<Object> requestResponseBodyAdvice = new ArrayList<>();

    @Nullable
    protected WebBindingInitializer webBindingInitializer;

    protected AsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("MvcAsync");

    @Nullable
    protected Long asyncRequestTimeout;

    protected CallableProcessingInterceptor[] callableInterceptors = new CallableProcessingInterceptor[0];

    protected DeferredResultProcessingInterceptor[] deferredResultInterceptors = new DeferredResultProcessingInterceptor[0];

    protected ReactiveAdapterRegistry reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();

    protected boolean ignoreDefaultModelOnRedirect = false;

    protected int cacheSecondsForSessionAttributeHandlers = 0;

    protected boolean synchronizeOnSession = false;

    protected SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();

    protected ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Nullable
    protected ConfigurableBeanFactory beanFactory;

    protected final Map<Class<?>, SessionAttributesHandler> sessionAttributesHandlerCache = new ConcurrentHashMap<>(64);

    protected final Map<Class<?>, Set<Method>> initBinderCache = new ConcurrentHashMap<>(64);

    protected final Map<ControllerAdviceBean, Set<Method>> initBinderAdviceCache = new LinkedHashMap<>();

    protected final Map<Class<?>, Set<Method>> modelAttributeCache = new ConcurrentHashMap<>(64);

    protected final Map<ControllerAdviceBean, Set<Method>> modelAttributeAdviceCache = new LinkedHashMap<>();

    /**
     * Provide resolvers for custom argument types. Custom resolvers are ordered
     * after built-in ones. To override the built-in support for argument
     * resolution use {@link #setArgumentResolvers} instead.
     */
    @Override
    public void setCustomArgumentResolvers(@Nullable List<HandlerMethodArgumentResolver> argumentResolvers) {
        this.customArgumentResolvers = argumentResolvers;
    }

    /**
     * Return the custom argument resolvers, or {@code null}.
     */
    @Override
    @Nullable
    public List<HandlerMethodArgumentResolver> getCustomArgumentResolvers() {
        return this.customArgumentResolvers;
    }

    /**
     * Configure the complete list of supported argument types thus overriding
     * the resolvers that would otherwise be configured by default.
     */
    @Override
    public void setArgumentResolvers(@Nullable List<HandlerMethodArgumentResolver> argumentResolvers) {
        if (argumentResolvers == null) {
            this.argumentResolvers = null;
        } else {
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite();
            this.argumentResolvers.addResolvers(argumentResolvers);
        }
    }

    /**
     * Return the configured argument resolvers, or possibly {@code null} if
     * not initialized yet via {@link #afterPropertiesSet()}.
     */
    @Override
    @Nullable
    public List<HandlerMethodArgumentResolver> getArgumentResolvers() {
        return (this.argumentResolvers != null ? this.argumentResolvers.getResolvers() : null);
    }

    /**
     * Configure the supported argument types in {@code @InitBinder} methods.
     */
    @Override
    public void setInitBinderArgumentResolvers(@Nullable List<HandlerMethodArgumentResolver> argumentResolvers) {
        if (argumentResolvers == null) {
            this.initBinderArgumentResolvers = null;
        } else {
            this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite();
            this.initBinderArgumentResolvers.addResolvers(argumentResolvers);
        }
    }

    /**
     * Return the argument resolvers for {@code @InitBinder} methods, or possibly
     * {@code null} if not initialized yet via {@link #afterPropertiesSet()}.
     */
    @Override
    @Nullable
    public List<HandlerMethodArgumentResolver> getInitBinderArgumentResolvers() {
        return (this.initBinderArgumentResolvers != null ? this.initBinderArgumentResolvers.getResolvers() : null);
    }

    /**
     * Provide handlers for custom return value types. Custom handlers are
     * ordered after built-in ones. To override the built-in support for
     * return value handling use {@link #setReturnValueHandlers}.
     */
    @Override
    public void setCustomReturnValueHandlers(@Nullable List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        this.customReturnValueHandlers = returnValueHandlers;
    }

    /**
     * Return the custom return value handlers, or {@code null}.
     */
    @Override
    @Nullable
    public List<HandlerMethodReturnValueHandler> getCustomReturnValueHandlers() {
        return this.customReturnValueHandlers;
    }

    /**
     * Configure the complete list of supported return value types thus
     * overriding handlers that would otherwise be configured by default.
     */
    @Override
    public void setReturnValueHandlers(@Nullable List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        if (returnValueHandlers == null) {
            this.returnValueHandlers = null;
        } else {
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite();
            this.returnValueHandlers.addHandlers(returnValueHandlers);
        }
    }

    /**
     * Return the configured handlers, or possibly {@code null} if not
     * initialized yet via {@link #afterPropertiesSet()}.
     */
    @Override
    @Nullable
    public List<HandlerMethodReturnValueHandler> getReturnValueHandlers() {
        return (this.returnValueHandlers != null ? this.returnValueHandlers.getHandlers() : null);
    }

    /**
     * Provide custom {@link ModelAndViewResolver ModelAndViewResolvers}.
     * <p><strong>Note:</strong> This method is available for backwards
     * compatibility only. However, it is recommended to re-write a
     * {@code ModelAndViewResolver} as {@link HandlerMethodReturnValueHandler}.
     * An adapter between the two interfaces is not possible since the
     * {@link HandlerMethodReturnValueHandler#supportsReturnType} method
     * cannot be implemented. Hence {@code ModelAndViewResolver}s are limited
     * to always being invoked at the end after all other return value
     * handlers have been given a chance.
     * <p>A {@code HandlerMethodReturnValueHandler} provides better access to
     * the return type and controller method information and can be ordered
     * freely relative to other return value handlers.
     */
    @Override
    public void setModelAndViewResolvers(@Nullable List<ModelAndViewResolver> modelAndViewResolvers) {
        this.modelAndViewResolvers = modelAndViewResolvers;
    }

    /**
     * Return the configured {@link ModelAndViewResolver ModelAndViewResolvers}, or {@code null}.
     */
    @Override
    @Nullable
    public List<ModelAndViewResolver> getModelAndViewResolvers() {
        return this.modelAndViewResolvers;
    }

    /**
     * Set the {@link ContentNegotiationManager} to use to determine requested media types.
     * If not set, the default constructor is used.
     */
    @Override
    public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager;
    }

    /**
     * Provide the converters to use in argument resolvers and return value
     * handlers that support reading and/or writing to the body of the
     * request and response.
     */
    @Override
    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
    }

    /**
     * Return the configured message body converters.
     */
    @Override
    public List<HttpMessageConverter<?>> getMessageConverters() {
        return this.messageConverters;
    }

    /**
     * Add one or more {@code RequestBodyAdvice} instances to intercept the
     * request before it is read and converted for {@code @RequestBody} and
     * {@code HttpEntity} method arguments.
     */
    @Override
    public void setRequestBodyAdvice(@Nullable List<RequestBodyAdvice> requestBodyAdvice) {
        if (requestBodyAdvice != null) {
            this.requestResponseBodyAdvice.addAll(requestBodyAdvice);
        }
    }

    /**
     * Add one or more {@code ResponseBodyAdvice} instances to intercept the
     * response before {@code @ResponseBody} or {@code ResponseEntity} return
     * values are written to the response body.
     */
    @Override
    public void setResponseBodyAdvice(@Nullable List<ResponseBodyAdvice<?>> responseBodyAdvice) {
        if (responseBodyAdvice != null) {
            this.requestResponseBodyAdvice.addAll(responseBodyAdvice);
        }
    }

    /**
     * Provide a WebBindingInitializer with "global" initialization to apply
     * to every DataBinder instance.
     */
    @Override
    public void setWebBindingInitializer(@Nullable WebBindingInitializer webBindingInitializer) {
        this.webBindingInitializer = webBindingInitializer;
    }

    /**
     * Return the configured WebBindingInitializer, or {@code null} if none.
     */
    @Override
    @Nullable
    public WebBindingInitializer getWebBindingInitializer() {
        return this.webBindingInitializer;
    }

    /**
     * Set the default {@link AsyncTaskExecutor} to use when a controller method
     * return a {@link Callable}. Controller methods can override this default on
     * a per-request basis by returning an {@link WebAsyncTask}.
     * <p>By default a {@link SimpleAsyncTaskExecutor} instance is used.
     * It's recommended to change that default in production as the simple executor
     * does not re-use threads.
     */
    @Override
    public void setTaskExecutor(AsyncTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * Specify the amount of time, in milliseconds, before concurrent handling
     * should time out. In Servlet 3, the timeout begins after the main request
     * processing thread has exited and ends when the request is dispatched again
     * for further processing of the concurrently produced result.
     * <p>If this value is not set, the default timeout of the underlying
     * implementation is used.
     *
     * @param timeout the timeout value in milliseconds
     */
    @Override
    public void setAsyncRequestTimeout(long timeout) {
        this.asyncRequestTimeout = timeout;
    }

    /**
     * Configure {@code CallableProcessingInterceptor}'s to register on async requests.
     *
     * @param interceptors the interceptors to register
     */
    @Override
    public void setCallableInterceptors(List<CallableProcessingInterceptor> interceptors) {
        this.callableInterceptors = interceptors.toArray(new CallableProcessingInterceptor[0]);
    }

    /**
     * Configure {@code DeferredResultProcessingInterceptor}'s to register on async requests.
     *
     * @param interceptors the interceptors to register
     */
    @Override
    public void setDeferredResultInterceptors(List<DeferredResultProcessingInterceptor> interceptors) {
        this.deferredResultInterceptors = interceptors.toArray(new DeferredResultProcessingInterceptor[0]);
    }

    /**
     * Configure the registry for reactive library types to be supported as
     * return values from controller methods.
     *
     * @since 5.0.5
     */
    @Override
    public void setReactiveAdapterRegistry(ReactiveAdapterRegistry reactiveAdapterRegistry) {
        this.reactiveAdapterRegistry = reactiveAdapterRegistry;
    }

    /**
     * Return the configured reactive type registry of adapters.
     *
     * @since 5.0
     */
    @Override
    public ReactiveAdapterRegistry getReactiveAdapterRegistry() {
        return this.reactiveAdapterRegistry;
    }

    /**
     * By default the content of the "default" model is used both during
     * rendering and redirect scenarios. Alternatively a controller method
     * can declare a {@link RedirectAttributes} argument and use it to provide
     * attributes for a redirect.
     * <p>Setting this flag to {@code true} guarantees the "default" model is
     * never used in a redirect scenario even if a RedirectAttributes argument
     * is not declared. Setting it to {@code false} means the "default" model
     * may be used in a redirect if the controller method doesn't declare a
     * RedirectAttributes argument.
     * <p>The default setting is {@code false} but new applications should
     * consider setting it to {@code true}.
     *
     * @see RedirectAttributes
     */
    @Override
    public void setIgnoreDefaultModelOnRedirect(boolean ignoreDefaultModelOnRedirect) {
        this.ignoreDefaultModelOnRedirect = ignoreDefaultModelOnRedirect;
    }

    /**
     * Specify the strategy to store session attributes with. The default is
     * {@link DefaultSessionAttributeStore},
     * storing session attributes in the HttpSession with the same attribute
     * name as in the model.
     */
    @Override
    public void setSessionAttributeStore(SessionAttributeStore sessionAttributeStore) {
        this.sessionAttributeStore = sessionAttributeStore;
    }

    /**
     * Cache content produced by {@code @SessionAttributes} annotated handlers
     * for the given number of seconds.
     * <p>Possible values are:
     * <ul>
     * <li>-1: no generation of cache-related headers</li>
     * <li>0 (default value): "Cache-Control: no-store" will prevent caching</li>
     * <li>1 or higher: "Cache-Control: max-age=seconds" will ask to cache content;
     * not advised when dealing with session attributes</li>
     * </ul>
     * <p>In contrast to the "cacheSeconds" property which will apply to all general
     * handlers (but not to {@code @SessionAttributes} annotated handlers),
     * this setting will apply to {@code @SessionAttributes} handlers only.
     *
     * @see #setCacheSeconds
     * @see org.springframework.web.bind.annotation.SessionAttributes
     */
    @Override
    public void setCacheSecondsForSessionAttributeHandlers(int cacheSecondsForSessionAttributeHandlers) {
        this.cacheSecondsForSessionAttributeHandlers = cacheSecondsForSessionAttributeHandlers;
    }

    /**
     * Set if controller execution should be synchronized on the session,
     * to serialize parallel invocations from the same client.
     * <p>More specifically, the execution of the {@code handleRequestInternal}
     * method will get synchronized if this flag is "true". The best available
     * session mutex will be used for the synchronization; ideally, this will
     * be a mutex exposed by HttpSessionMutexListener.
     * <p>The session mutex is guaranteed to be the same object during
     * the entire lifetime of the session, available under the key defined
     * by the {@code SESSION_MUTEX_ATTRIBUTE} constant. It serves as a
     * safe reference to synchronize on for locking on the current session.
     * <p>In many cases, the HttpSession reference itself is a safe mutex
     * as well, since it will always be the same object reference for the
     * same active logical session. However, this is not guaranteed across
     * different servlet containers; the only 100% safe way is a session mutex.
     *
     * @see org.springframework.web.util.HttpSessionMutexListener
     * @see WebUtils#getSessionMutex(HttpSession)
     */
    @Override
    public void setSynchronizeOnSession(boolean synchronizeOnSession) {
        this.synchronizeOnSession = synchronizeOnSession;
    }

    /**
     * Set the ParameterNameDiscoverer to use for resolving method parameter names if needed
     * (e.g. for default attribute names).
     * <p>Default is a {@link DefaultParameterNameDiscoverer}.
     */
    @Override
    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    /**
     * A {@link ConfigurableBeanFactory} is expected for resolving expressions
     * in method argument default values.
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    /**
     * Return the owning factory of this bean instance, or {@code null} if none.
     */
    @Override
    @Nullable
    protected ConfigurableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }


    @Override
    public void afterPropertiesSet() {
        initControllerAdviceCache();

        RequestParamMapMethodArgumentResolver requestParamMapMethodArgumentResolver = new RequestParamMapMethodArgumentResolver();
        RequestResponseBodyMethodProcessor requestResponseBodyMethodProcessor = new RequestResponseBodyMethodProcessor(this.getMessageConverters(),
                this.contentNegotiationManager, this.requestResponseBodyAdvice);
        PathVariableMethodArgumentResolver pathVariableMethodArgumentResolver = new PathVariableMethodArgumentResolver();
        PathVariableMapMethodArgumentResolver pathVariableMapMethodArgumentResolver = new PathVariableMapMethodArgumentResolver();
        MatrixVariableMethodArgumentResolver matrixVariableMethodArgumentResolver = new MatrixVariableMethodArgumentResolver();
        MatrixVariableMapMethodArgumentResolver matrixVariableMapMethodArgumentResolver = new MatrixVariableMapMethodArgumentResolver();
        ExpressionValueMethodArgumentResolver expressionValueMethodArgumentResolver =
                new ExpressionValueMethodArgumentResolver(this.getBeanFactory());
        SessionAttributeMethodArgumentResolver sessionAttributeMethodArgumentResolver = new SessionAttributeMethodArgumentResolver();
        RequestAttributeMethodArgumentResolver requestAttributeMethodArgumentResolver = new RequestAttributeMethodArgumentResolver();
        ServletRequestMethodArgumentResolver servletRequestMethodArgumentResolver = new ServletRequestMethodArgumentResolver();
        ServletResponseMethodArgumentResolver servletResponseMethodArgumentResolver = new ServletResponseMethodArgumentResolver();
        HttpEntityMethodProcessor httpEntityMethodProcessor = new HttpEntityMethodProcessor(this.getMessageConverters(), this.contentNegotiationManager, this.requestResponseBodyAdvice);
        ModelMethodProcessor modelMethodProcessor = new ModelMethodProcessor();


        if (this.argumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers(
                    requestParamMapMethodArgumentResolver,
                    requestResponseBodyMethodProcessor,
                    pathVariableMethodArgumentResolver,
                    pathVariableMapMethodArgumentResolver,
                    matrixVariableMethodArgumentResolver,
                    matrixVariableMapMethodArgumentResolver,
                    expressionValueMethodArgumentResolver,
                    sessionAttributeMethodArgumentResolver,
                    requestAttributeMethodArgumentResolver,
                    servletRequestMethodArgumentResolver,
                    servletResponseMethodArgumentResolver,
                    httpEntityMethodProcessor,
                    modelMethodProcessor
            );
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }
        if (this.initBinderArgumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = this.getDefaultInitBinderArgumentResolvers(
                    requestParamMapMethodArgumentResolver,
                    pathVariableMethodArgumentResolver,
                    pathVariableMapMethodArgumentResolver,
                    matrixVariableMethodArgumentResolver,
                    matrixVariableMapMethodArgumentResolver,
                    expressionValueMethodArgumentResolver,
                    sessionAttributeMethodArgumentResolver,
                    requestAttributeMethodArgumentResolver,
                    servletRequestMethodArgumentResolver,
                    servletResponseMethodArgumentResolver
            );
            this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }
        if (this.returnValueHandlers == null) {
            List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers(
                    requestResponseBodyMethodProcessor,
                    httpEntityMethodProcessor,
                    modelMethodProcessor
            );
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
        }
    }

    protected void initControllerAdviceCache() {
        if (getApplicationContext() == null) {
            return;
        }

        List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(getApplicationContext());

        List<Object> requestResponseBodyAdviceBeans = new ArrayList<>();

        for (ControllerAdviceBean adviceBean : adviceBeans) {
            Class<?> beanType = adviceBean.getBeanType();
            if (beanType == null) {
                throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
            }
            Set<Method> attrMethods = MethodIntrospector.selectMethods(beanType, MODEL_ATTRIBUTE_METHODS);
            if (!attrMethods.isEmpty()) {
                this.modelAttributeAdviceCache.put(adviceBean, attrMethods);
            }
            Set<Method> binderMethods = MethodIntrospector.selectMethods(beanType, INIT_BINDER_METHODS);
            if (!binderMethods.isEmpty()) {
                this.initBinderAdviceCache.put(adviceBean, binderMethods);
            }
            if (RequestBodyAdvice.class.isAssignableFrom(beanType) || ResponseBodyAdvice.class.isAssignableFrom(beanType)) {
                requestResponseBodyAdviceBeans.add(adviceBean);
            }
        }

        if (!requestResponseBodyAdviceBeans.isEmpty()) {
            this.requestResponseBodyAdvice.addAll(0, requestResponseBodyAdviceBeans);
        }

        if (logger.isDebugEnabled()) {
            int modelSize = this.modelAttributeAdviceCache.size();
            int binderSize = this.initBinderAdviceCache.size();
            int reqCount = getBodyAdviceCount(RequestBodyAdvice.class);
            int resCount = getBodyAdviceCount(ResponseBodyAdvice.class);
            if (modelSize == 0 && binderSize == 0 && reqCount == 0 && resCount == 0) {
                logger.debug("ControllerAdvice beans: none");
            } else {
                logger.debug("ControllerAdvice beans: " + modelSize + " @ModelAttribute, " + binderSize +
                        " @InitBinder, " + reqCount + " RequestBodyAdvice, " + resCount + " ResponseBodyAdvice");
            }
        }
    }

    // Count all advice, including explicit registrations..

    protected int getBodyAdviceCount(Class<?> adviceType) {
        List<Object> advice = this.requestResponseBodyAdvice;
        return RequestBodyAdvice.class.isAssignableFrom(adviceType) ?
                SpringWrapperUtil.callAdviceByType(advice, RequestBodyAdvice.class).size() :
                SpringWrapperUtil.callAdviceByType(advice, ResponseBodyAdvice.class).size();
    }

    /**
     * Return the list of argument resolvers to use including built-in resolvers
     * and custom resolvers provided via {@link #setCustomArgumentResolvers}.
     *
     * @param requestParamMapMethodArgumentResolver
     * @param requestResponseBodyMethodProcessor
     */
    protected List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers(
            RequestParamMapMethodArgumentResolver requestParamMapMethodArgumentResolver,
            RequestResponseBodyMethodProcessor requestResponseBodyMethodProcessor,
            PathVariableMethodArgumentResolver pathVariableMethodArgumentResolver,
            PathVariableMapMethodArgumentResolver pathVariableMapMethodArgumentResolver,
            MatrixVariableMethodArgumentResolver matrixVariableMethodArgumentResolver,
            MatrixVariableMapMethodArgumentResolver matrixVariableMapMethodArgumentResolver,
            ExpressionValueMethodArgumentResolver expressionValueMethodArgumentResolver,
            SessionAttributeMethodArgumentResolver sessionAttributeMethodArgumentResolver,
            RequestAttributeMethodArgumentResolver requestAttributeMethodArgumentResolver,
            ServletRequestMethodArgumentResolver servletRequestMethodArgumentResolver,
            ServletResponseMethodArgumentResolver servletResponseMethodArgumentResolver,
            HttpEntityMethodProcessor httpEntityMethodProcessor,
            ModelMethodProcessor modelMethodProcessor
    ) {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>(30);

        // Annotation-based argument resolution
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
        resolvers.add(requestParamMapMethodArgumentResolver);
        resolvers.add(pathVariableMethodArgumentResolver);
        resolvers.add(pathVariableMapMethodArgumentResolver);
        resolvers.add(matrixVariableMethodArgumentResolver);
        resolvers.add(matrixVariableMapMethodArgumentResolver);
        resolvers.add(new ServletModelAttributeMethodProcessor(false));
        resolvers.add(requestResponseBodyMethodProcessor);
        resolvers.add(new RequestPartMethodArgumentResolver(getMessageConverters(), this.requestResponseBodyAdvice));
        resolvers.add(new RequestHeaderMethodArgumentResolver(getBeanFactory()));
        resolvers.add(new RequestHeaderMapMethodArgumentResolver());
        resolvers.add(new ServletCookieValueMethodArgumentResolver(getBeanFactory()));
        resolvers.add(expressionValueMethodArgumentResolver);
        resolvers.add(sessionAttributeMethodArgumentResolver);
        resolvers.add(requestAttributeMethodArgumentResolver);

        // Type-based argument resolution
        resolvers.add(servletRequestMethodArgumentResolver);
        resolvers.add(servletResponseMethodArgumentResolver);
        resolvers.add(httpEntityMethodProcessor);
        resolvers.add(new RedirectAttributesMethodArgumentResolver());
        resolvers.add(modelMethodProcessor);
        resolvers.add(new MapMethodProcessor());
        resolvers.add(new ErrorsMethodArgumentResolver());
        resolvers.add(new SessionStatusMethodArgumentResolver());
        resolvers.add(new UriComponentsBuilderMethodArgumentResolver());

        // Custom arguments
        if (getCustomArgumentResolvers() != null) {
            resolvers.addAll(getCustomArgumentResolvers());
        }

        // Catch-all
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
        resolvers.add(new ServletModelAttributeMethodProcessor(true));

        return resolvers;
    }

    /**
     * Return the list of argument resolvers to use for {@code @InitBinder}
     * methods including built-in and custom resolvers.
     */
    protected List<HandlerMethodArgumentResolver> getDefaultInitBinderArgumentResolvers(
            RequestParamMapMethodArgumentResolver requestParamMapMethodArgumentResolver,
            PathVariableMethodArgumentResolver pathVariableMethodArgumentResolver,
            PathVariableMapMethodArgumentResolver pathVariableMapMethodArgumentResolver,
            MatrixVariableMethodArgumentResolver matrixVariableMethodArgumentResolver,
            MatrixVariableMapMethodArgumentResolver matrixVariableMapMethodArgumentResolver,
            ExpressionValueMethodArgumentResolver expressionValueMethodArgumentResolver,
            SessionAttributeMethodArgumentResolver sessionAttributeMethodArgumentResolver,
            RequestAttributeMethodArgumentResolver requestAttributeMethodArgumentResolver,
            ServletRequestMethodArgumentResolver servletRequestMethodArgumentResolver,
            ServletResponseMethodArgumentResolver servletResponseMethodArgumentResolver
    ) {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // Annotation-based argument resolution
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
        resolvers.add(requestParamMapMethodArgumentResolver);
        resolvers.add(pathVariableMethodArgumentResolver);
        resolvers.add(pathVariableMapMethodArgumentResolver);
        resolvers.add(matrixVariableMethodArgumentResolver);
        resolvers.add(matrixVariableMapMethodArgumentResolver);
        resolvers.add(expressionValueMethodArgumentResolver);
        resolvers.add(sessionAttributeMethodArgumentResolver);
        resolvers.add(requestAttributeMethodArgumentResolver);

        // Type-based argument resolution
        resolvers.add(servletRequestMethodArgumentResolver);
        resolvers.add(servletResponseMethodArgumentResolver);

        // Custom arguments
        if (getCustomArgumentResolvers() != null) {
            resolvers.addAll(getCustomArgumentResolvers());
        }

        // Catch-all
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));

        return resolvers;
    }

    /**
     * Return the list of return value handlers to use including built-in and
     * custom handlers provided via {@link #setReturnValueHandlers}.
     */
    protected List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers(
            RequestResponseBodyMethodProcessor requestResponseBodyMethodProcessor,
            HttpEntityMethodProcessor httpEntityMethodProcessor,
            ModelMethodProcessor modelMethodProcessor
    ) {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();

        // 框架功能相关的返回值
        handlers.add(new ModelAndViewMethodReturnValueHandler());
        handlers.add(modelMethodProcessor);
        handlers.add(new ViewMethodReturnValueHandler());
        handlers.add(new ResponseBodyEmitterReturnValueHandler(getMessageConverters(),
                this.reactiveAdapterRegistry, this.taskExecutor, this.contentNegotiationManager));
        handlers.add(new StreamingResponseBodyReturnValueHandler());
        handlers.add(httpEntityMethodProcessor);
        handlers.add(new HttpHeadersReturnValueHandler());
        handlers.add(new CallableMethodReturnValueHandler());
        handlers.add(new DeferredResultMethodReturnValueHandler());
        handlers.add(new AsyncTaskMethodReturnValueHandler(this.beanFactory));

        // 基于注解的返回值
        handlers.add(new ModelAttributeMethodProcessor(false));
        handlers.add(requestResponseBodyMethodProcessor);

        // 处理简单类型
        handlers.add(new SimpleTypeHandler(new ViewNameMethodReturnValueHandler(), requestResponseBodyMethodProcessor));

        // 添加自定义类型
        if (getCustomReturnValueHandlers() != null) {
            handlers.addAll(getCustomReturnValueHandlers());
        }

        // 处理所有的类型
        RestWebProperties properties = getApplicationContext().getBean(RestWebProperties.class);
        handlers.add(new RestHandler(requestResponseBodyMethodProcessor, properties));

        return handlers;
    }


    /**
     * Always return {@code true} since any method argument and return value
     * type will be processed in some way. A method argument not recognized
     * by any HandlerMethodArgumentResolver is interpreted as a request parameter
     * if it is a simple type, or as a model attribute otherwise. A return value
     * not recognized by any HandlerMethodReturnValueHandler will be interpreted
     * as a model attribute.
     */
    @Override
    protected boolean supportsInternal(HandlerMethod handlerMethod) {
        return true;
    }

    @Override
    protected ModelAndView handleInternal(HttpServletRequest request,
                                          HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

        ModelAndView mav;
        checkRequest(request);

        // Execute invokeHandlerMethod in synchronized block if required.
        if (this.synchronizeOnSession) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object mutex = WebUtils.getSessionMutex(session);
                synchronized (mutex) {
                    mav = invokeHandlerMethod(request, response, handlerMethod);
                }
            } else {
                // No HttpSession available -> no mutex necessary
                mav = invokeHandlerMethod(request, response, handlerMethod);
            }
        } else {
            // No synchronization on session demanded at all...
            mav = invokeHandlerMethod(request, response, handlerMethod);
        }

        if (!response.containsHeader(HEADER_CACHE_CONTROL)) {
            if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
                applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
            } else {
                prepareResponse(response);
            }
        }

        return mav;
    }

    /**
     * This implementation always returns -1. An {@code @RequestMapping} method can
     * calculate the lastModified value, call {@link WebRequest#checkNotModified(long)},
     * and return {@code null} if the result of that call is {@code true}.
     */
    @Override
    protected long getLastModifiedInternal(HttpServletRequest request, HandlerMethod handlerMethod) {
        return -1;
    }


    /**
     * Return the {@link SessionAttributesHandler} instance for the given handler type
     * (never {@code null}).
     */
    protected SessionAttributesHandler getSessionAttributesHandler(HandlerMethod handlerMethod) {
        Class<?> handlerType = handlerMethod.getBeanType();
        SessionAttributesHandler sessionAttrHandler = this.sessionAttributesHandlerCache.get(handlerType);
        if (sessionAttrHandler == null) {
            synchronized (this.sessionAttributesHandlerCache) {
                sessionAttrHandler = this.sessionAttributesHandlerCache.get(handlerType);
                if (sessionAttrHandler == null) {
                    sessionAttrHandler = new SessionAttributesHandler(handlerType, this.sessionAttributeStore);
                    this.sessionAttributesHandlerCache.put(handlerType, sessionAttrHandler);
                }
            }
        }
        return sessionAttrHandler;
    }

    /**
     * Invoke the {@link RequestMapping} handler method preparing a {@link ModelAndView}
     * if view resolution is required.
     *
     * @see #createInvocableHandlerMethod(HandlerMethod)
     * @since 4.2
     */
    @Override
    @Nullable
    protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
                                               HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        try {
            WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
            ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);

            ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
            if (this.argumentResolvers != null) {
                invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
            }
            if (this.returnValueHandlers != null) {
                invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
            }
            invocableMethod.setDataBinderFactory(binderFactory);
            invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

            ModelAndViewContainer mavContainer = new ModelAndViewContainer();
            mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
            modelFactory.initModel(webRequest, mavContainer, invocableMethod);
            mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

            AsyncWebRequest asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response);
            asyncWebRequest.setTimeout(this.asyncRequestTimeout);

            WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
            asyncManager.setTaskExecutor(this.taskExecutor);
            asyncManager.setAsyncWebRequest(asyncWebRequest);
            asyncManager.registerCallableInterceptors(this.callableInterceptors);
            asyncManager.registerDeferredResultInterceptors(this.deferredResultInterceptors);

            if (asyncManager.hasConcurrentResult()) {
                Object result = asyncManager.getConcurrentResult();
                mavContainer = (ModelAndViewContainer) asyncManager.getConcurrentResultContext()[0];
                asyncManager.clearConcurrentResult();
                LogFormatUtils.traceDebug(logger, traceOn -> {
                    String formatted = LogFormatUtils.formatValue(result, !traceOn);
                    return "Resume with async result [" + formatted + "]";
                });
                invocableMethod = SpringWrapperUtil.callWrapConcurrentResult(invocableMethod, result);
            }

            invocableMethod.invokeAndHandle(webRequest, mavContainer);
            if (asyncManager.isConcurrentHandlingStarted()) {
                return null;
            }

            return getModelAndView(mavContainer, modelFactory, webRequest);
        } finally {
            webRequest.requestCompleted();
        }
    }

    /**
     * Create a {@link ServletInvocableHandlerMethod} from the given {@link HandlerMethod} definition.
     *
     * @param handlerMethod the {@link HandlerMethod} definition
     * @return the corresponding {@link ServletInvocableHandlerMethod} (or custom subclass thereof)
     * @since 4.2
     */
    @Override
    protected ServletInvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod) {
        return new ServletInvocableHandlerMethod(handlerMethod);
    }


    protected ModelFactory getModelFactory(HandlerMethod handlerMethod, WebDataBinderFactory binderFactory) {
        SessionAttributesHandler sessionAttrHandler = getSessionAttributesHandler(handlerMethod);
        Class<?> handlerType = handlerMethod.getBeanType();
        Set<Method> methods = this.modelAttributeCache.get(handlerType);
        if (methods == null) {
            methods = MethodIntrospector.selectMethods(handlerType, MODEL_ATTRIBUTE_METHODS);
            this.modelAttributeCache.put(handlerType, methods);
        }
        List<InvocableHandlerMethod> attrMethods = new ArrayList<>();
        // Global methods first
        this.modelAttributeAdviceCache.forEach((controllerAdviceBean, methodSet) -> {
            if (controllerAdviceBean.isApplicableToBeanType(handlerType)) {
                Object bean = controllerAdviceBean.resolveBean();
                for (Method method : methodSet) {
                    attrMethods.add(createModelAttributeMethod(binderFactory, bean, method));
                }
            }
        });
        for (Method method : methods) {
            Object bean = handlerMethod.getBean();
            attrMethods.add(createModelAttributeMethod(binderFactory, bean, method));
        }
        return new ModelFactory(attrMethods, binderFactory, sessionAttrHandler);
    }

    protected InvocableHandlerMethod createModelAttributeMethod(WebDataBinderFactory factory, Object bean, Method method) {
        InvocableHandlerMethod attrMethod = new InvocableHandlerMethod(bean, method);
        if (this.argumentResolvers != null) {
            attrMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        }
        attrMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
        attrMethod.setDataBinderFactory(factory);
        return attrMethod;
    }

    protected WebDataBinderFactory getDataBinderFactory(HandlerMethod handlerMethod) throws Exception {
        Class<?> handlerType = handlerMethod.getBeanType();
        Set<Method> methods = this.initBinderCache.get(handlerType);
        if (methods == null) {
            methods = MethodIntrospector.selectMethods(handlerType, INIT_BINDER_METHODS);
            this.initBinderCache.put(handlerType, methods);
        }
        List<InvocableHandlerMethod> initBinderMethods = new ArrayList<>();
        // Global methods first
        this.initBinderAdviceCache.forEach((controllerAdviceBean, methodSet) -> {
            if (controllerAdviceBean.isApplicableToBeanType(handlerType)) {
                Object bean = controllerAdviceBean.resolveBean();
                for (Method method : methodSet) {
                    initBinderMethods.add(createInitBinderMethod(bean, method));
                }
            }
        });
        for (Method method : methods) {
            Object bean = handlerMethod.getBean();
            initBinderMethods.add(createInitBinderMethod(bean, method));
        }
        return createDataBinderFactory(initBinderMethods);
    }

    protected InvocableHandlerMethod createInitBinderMethod(Object bean, Method method) {
        InvocableHandlerMethod binderMethod = new InvocableHandlerMethod(bean, method);
        if (this.initBinderArgumentResolvers != null) {
            binderMethod.setHandlerMethodArgumentResolvers(this.initBinderArgumentResolvers);
        }
        binderMethod.setDataBinderFactory(new DefaultDataBinderFactory(this.webBindingInitializer));
        binderMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
        return binderMethod;
    }

    /**
     * Template method to create a new InitBinderDataBinderFactory instance.
     * <p>The default implementation creates a ServletRequestDataBinderFactory.
     * This can be overridden for custom ServletRequestDataBinder subclasses.
     *
     * @param binderMethods {@code @InitBinder} methods
     * @return the InitBinderDataBinderFactory instance to use
     * @throws Exception in case of invalid state or arguments
     */
    @Override
    protected InitBinderDataBinderFactory createDataBinderFactory(List<InvocableHandlerMethod> binderMethods)
            throws Exception {

        return new ServletRequestDataBinderFactory(binderMethods, getWebBindingInitializer());
    }

    @Nullable
    protected ModelAndView getModelAndView(ModelAndViewContainer mavContainer,
                                           ModelFactory modelFactory, NativeWebRequest webRequest) throws Exception {

        modelFactory.updateModel(webRequest, mavContainer);
        if (mavContainer.isRequestHandled()) {
            return null;
        }
        ModelMap model = mavContainer.getModel();
        ModelAndView mav = new ModelAndView(mavContainer.getViewName(), model, mavContainer.getStatus());
        if (!mavContainer.isViewReference()) {
            mav.setView((View) mavContainer.getView());
        }
        if (model instanceof RedirectAttributes) {
            Map<String, ?> flashAttributes = ((RedirectAttributes) model).getFlashAttributes();
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (request != null) {
                RequestContextUtils.getOutputFlashMap(request).putAll(flashAttributes);
            }
        }
        return mav;
    }

}
