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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
class ResourceController {

	private RestTemplate template;

	@Value("${app.services.resource}")
	private URI resourceUrl;

	ResourceController(RestTemplateBuilder builder) {
		template = builder.build();
	}

	@GetMapping("/resource/message")
	public byte[] resource() throws Exception {
		return template.exchange(RequestEntity.get(new URI(resourceUrl.toString() + "/message"))
				.accept(MediaType.TEXT_HTML).build(), byte[].class).getBody();
	}
}