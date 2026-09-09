package com.example.config.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void currentConfigurationReturnsDefaultProperties() throws Exception {
        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.module").value("02-config"))
                .andExpect(jsonPath("$.name").value("config-demo"))
                .andExpect(jsonPath("$.environment").value("default"))
                .andExpect(jsonPath("$.featuresEnabled").value(false))
                .andExpect(jsonPath("$.requestTimeoutMilliseconds").value(2000))
                .andExpect(jsonPath("$.allowedOrigins[0]").value("http://localhost:3000"))
                .andExpect(jsonPath("$.activeProfiles").isEmpty());
    }
}
