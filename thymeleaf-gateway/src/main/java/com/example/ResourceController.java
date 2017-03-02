/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.util.ParsingPathMatcher;

@RestController
class ResourceController extends WebMvcConfigurerAdapter {
    private RestTemplate template;

    @Value("${app.services.resource}")
    private URI resourceUrl;

    ResourceController(RestTemplateBuilder builder) {
        template = builder.build();
    }
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setPathMatcher(new ParsingPathMatcher());
        configurer.setUseSuffixPatternMatch(false);
        configurer.setUseTrailingSlashMatch(false);
    }

    @GetMapping("/resource/{*path}")
    public byte[] resource(@PathVariable String path) throws Exception {
        if ("".equals(path)) {
            path = "/resource";
        }
        return template
                .exchange(RequestEntity.get(new URI(resourceUrl.toString() + path))
                        .accept(MediaType.APPLICATION_JSON).build(), byte[].class)
                .getBody();
    }
}