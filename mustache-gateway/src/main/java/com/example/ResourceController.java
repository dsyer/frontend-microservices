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
import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.VersionResourceResolver;

@RestController
class ResourceController implements WebMvcConfigurer {

	private RestTemplate template;
	
	@Value("${app.services.user:dave}")
	private String user;

	@Value("${app.services.resource}")
	private URI resourceUrl;

	private ResourceProperties resources;

	ResourceController(RestTemplateBuilder builder, ResourceProperties resources) {
		this.resources = resources;
		template = builder.build();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resource/**")
				.addResourceLocations(resourceUrl.toString())
				.resourceChain(resources.getChain().isCache()).addResolver(
						new VersionResourceResolver().addContentVersionStrategy("/**"));
	}

	@GetMapping("/resource/**")
	public Object statics(@RequestAttribute(PathUtils.PATH_ATTR) String path, Principal principal) {
		String tail = PathUtils.tail(path);
		if (principal!=null && user.equals(principal.getName())) {
			try {
				return template.exchange(
						RequestEntity.get(new URI(resourceUrl.toString() + tail)).build(),
						byte[].class);
			}
			catch (Exception e) {
			}
		}
		return new ModelAndView(PathUtils.forward(tail));
	}

	@GetMapping("/resource")
	public byte[] resource() throws Exception {
		return template
				.exchange(RequestEntity.get(new URI(resourceUrl.toString() + "/resource"))
						.accept(MediaType.APPLICATION_JSON).build(), byte[].class)
				.getBody();
	}
}