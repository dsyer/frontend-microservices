package com.example;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureRestDocs(outputDir="target/snippets")
@AutoConfigureMockMvc
public class ResourceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void resource() throws Exception {
        mockMvc.perform(get("/message")).andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("Hello World")))
                .andDo(document("resource"));
    }

}
