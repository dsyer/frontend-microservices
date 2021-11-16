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

@ControllerAdvice
class LayoutAdvice {

    private final Mustache.Compiler compiler;

    private Application application;

    @Autowired
    public LayoutAdvice(Compiler compiler, Application application) {
        this.compiler = compiler;
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

    @ModelAttribute("layout")
    public Mustache.Lambda layout(Map<String, Object> model) {
        return new Layout(compiler);
    }
}