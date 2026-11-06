package top.cch.config.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "server.port=8002")
@AutoConfigureMockMvc
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void basicEndpointReturnsPortAndAppName() throws Exception {
        mockMvc.perform(get("/config/basic"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("8002"),
                        containsString("02-配置管理"))));
    }

    @Test
    void myEndpointReturnsNameAndJob() throws Exception {
        mockMvc.perform(get("/config/my"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("陈昶好"),
                        containsString("学生"))));
    }

    @Test
    void valueEndpointReturnsAllValueForms() throws Exception {
        mockMvc.perform(get("/config/value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.randomInt", allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(100))))
                .andExpect(jsonPath("$.author").value("陈昶好"))
                .andExpect(jsonPath("$.remark").value("SpringBoot配置学习"))
                .andExpect(jsonPath("$.randomUuid").value(not(emptyOrNullString())))
                .andExpect(jsonPath("$.adult").value("成年"));
    }

    @Test
    void appEndpointReturnsStudentProperties() throws Exception {
        mockMvc.perform(get("/config/app"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("陈昶好"))
                .andExpect(jsonPath("$.age").value(18))
                .andExpect(jsonPath("$.birthDate").value("2006-03-14"))
                .andExpect(jsonPath("$.address.city").value("南京市"))
                .andExpect(jsonPath("$.courses[0].name").value("高等数学"));
    }

    @Test
    void studentEndpointReturnsStudentProperties() throws Exception {
        mockMvc.perform(get("/student/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("陈昶好"))
                .andExpect(jsonPath("$.age").value(18))
                .andExpect(jsonPath("$.address.city").value("南京市"))
                .andExpect(jsonPath("$.courses[0].name").value("高等数学"));
    }

    @Test
    void envEndpointReturnsActiveProfileBean() throws Exception {
        mockMvc.perform(get("/config/env"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("dev"),
                        containsString("开发环境"),
                        containsString("我是 dev 环境专属的 Bean"))));
    }
}
