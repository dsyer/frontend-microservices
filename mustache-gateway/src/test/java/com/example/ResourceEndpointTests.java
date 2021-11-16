package com.example;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest("app.services.resource: http://localhost:${wiremock.server.port}")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0, stubs = "classpath:/META-INF/com.example/mustache-resource/0.0.1-SNAPSHOT")
public class ResourceEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void message() throws Exception {
        mockMvc.perform(get("/resource/message")).andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("<div class=\"container\"")));
    }

}
