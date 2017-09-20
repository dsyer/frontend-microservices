package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest("app.services.resource: http://localhost:${wiremock.server.port}")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port=0, stubs="classpath:/META-INF/com.example/mustache-resource/0.0.1-SNAPSHOT")
public class ResourceEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void resource() throws Exception {
        mockMvc.perform(get("/resource")).andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("Hello World")));
    }

    @Test
    @WithMockUser(username="dave")
    public void template() throws Exception {
        mockMvc.perform(get("/resource/templates/message.html")).andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("<div class=\"container\"")));
    }

    @Test
    @WithMockUser(username="sam")
    public void fallback() throws Exception {
        mockMvc.perform(get("/resource/templates/message.html")).andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("unavailable")));
    }

    @Test
    @WithMockUser(username="dave")
    public void app() throws Exception {
        mockMvc.perform(get("/resource/js/home.js")).andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("angular.module(\"home\"")));
    }

}
