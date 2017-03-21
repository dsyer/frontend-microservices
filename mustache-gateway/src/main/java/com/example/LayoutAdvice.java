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

import java.util.Map;

import com.example.Application.Menu;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

@ControllerAdvice
class LayoutAdvice {

    private final Mustache.Compiler compiler;

    private Application application;

    private ResourceUrlProvider urls;

    @Autowired
    public LayoutAdvice(Compiler compiler, ResourceUrlProvider urls, Application application) {
        this.compiler = compiler;
        this.urls = urls;
        this.application = application;
    }

    @ModelAttribute("menus")
    public Iterable<Menu> menus(@ModelAttribute Layout layout) {
        for (Menu menu : application.getMenus()) {
            menu.setActive(false);
        }
        return application.getMenus();
    }

    @ModelAttribute("menu")
    public Mustache.Lambda menu(@ModelAttribute Layout layout) {
        return (frag, out) -> {
            Menu menu = application.getMenu(frag.execute());
            menu.setActive(true);
            layout.title = menu.getTitle();
        };
    }

    @ModelAttribute("url")
    public Mustache.Lambda url() {
        return (frag, out) -> {
            String path = frag.execute();
            String url = urls.getForLookupPath(path);
            out.append(url !=null ? url : path);
        };
    }

    @ModelAttribute("script")
    public Mustache.Lambda script(@ModelAttribute Layout layout) {
        return (frag, out) -> {
            layout.script.add(frag.execute());
        };
    }

    @ModelAttribute("layout")
    public Mustache.Lambda layout(Map<String, Object> model) {
        return new Layout(compiler);
    }
}