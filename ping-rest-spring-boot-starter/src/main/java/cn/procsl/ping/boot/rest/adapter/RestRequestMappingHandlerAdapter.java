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

package cn.procsl.ping.boot.rest.adapter;

import cn.procsl.ping.boot.rest.hook.RequestAdapterHook;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

/**
 * @author procsl
 */
@RequiredArgsConstructor
public class RestRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

    final List<RequestAdapterHook> hooks;

    boolean once = true;

    @Override
    public List<HttpMessageConverter<?>> getMessageConverters() {
        List<HttpMessageConverter<?>> converters = super.getMessageConverters();
        if (!once) {
            return converters;
        }
        once = false;
        if (hooks != null) {
            for (RequestAdapterHook hook : hooks) {
                hook.httpMessageConverter(converters);
            }
        }
        return converters;
    }

}
