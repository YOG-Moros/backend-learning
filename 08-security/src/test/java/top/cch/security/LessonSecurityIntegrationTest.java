package top.cch.security;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"mqxu.security.sms.store=memory", "mqxu.security.sms.send-interval=1ms"})
@AutoConfigureMockMvc
class LessonSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    private String basic(String user) { return "Basic " + Base64.getEncoder().encodeToString((user + ":123456").getBytes(StandardCharsets.UTF_8)); }
    private String login(String user) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + user + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.authorities", hasItem("FACTOR_PASSWORD")))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.token");
    }
    @Test void stageOneRequiresBasicCredentials() throws Exception {
        mvc.perform(get("/stage1/hello")).andExpect(status().isUnauthorized());
        mvc.perform(get("/stage1/hello").header("Authorization", basic("alice"))).andExpect(status().isOk());
    }
    @Test void stageTwoProtectsWritesButAllowsReads() throws Exception {
        mvc.perform(get("/stage2/articles/1")).andExpect(status().isOk());
        mvc.perform(delete("/stage2/articles/1")).andExpect(status().isUnauthorized());
    }
    @Test void rolesAndPermissionsAreDifferent() throws Exception {
        mvc.perform(get("/stage3/audit/logs").header("Authorization", basic("carol"))).andExpect(status().isForbidden());
        mvc.perform(get("/stage3/audit/logs").header("Authorization", basic("bob"))).andExpect(status().isOk());
        mvc.perform(get("/stage3/ops/status").header("Authorization", basic("erin"))).andExpect(status().isOk());
        mvc.perform(delete("/stage3/articles/1").header("Authorization", basic("bob"))).andExpect(status().isForbidden());
    }
    @Test void jwtLoginAndAuthorization() throws Exception {
        String token = login("alice");
        mvc.perform(get("/api/hello").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/articles/1001").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/tasks").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
    @Test void malformedAndTamperedTokensAreUnauthorized() throws Exception {
        String token = login("alice");
        int index = token.lastIndexOf('.') + 1;
        String altered = token.substring(0,index) + (token.charAt(index)=='A'?'B':'A') + token.substring(index+1);
        mvc.perform(get("/api/hello").header("Authorization", "Bearer " + altered)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/hello").header("Authorization", "Bearer bad")).andExpect(status().isUnauthorized());
    }
    @Test void disabledAndLockedAccountsReturnForbidden() throws Exception {
        for (String user : new String[]{"dave","frank"}) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\""+user+"\",\"password\":\"123456\"}"))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value(403));
        }
    }
    @Test void smsIsOneTimeAndChecksAccountState() throws Exception {
        String body = mvc.perform(post("/api/auth/sms/code").param("phone","13900001111"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String code = java.util.regex.Pattern.compile("\\d{6}").matcher(body).results().findFirst().orElseThrow().group();
        String request = "{\"phone\":\"13900001111\",\"code\":\""+code+"\"}";
        mvc.perform(post("/api/auth/sms/login").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isString());
        mvc.perform(post("/api/auth/sms/login").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isUnauthorized());
    }
    @Test void preflightAndUnauthorizedResponseHaveCorsHeaders() throws Exception {
        mvc.perform(options("/api/hello").header("Origin","http://localhost:5173").header("Access-Control-Request-Method","GET"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
        mvc.perform(get("/api/hello").header("Origin","http://localhost:5173"))
                .andExpect(status().isUnauthorized()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
    }
    @Test void formLoginRequiresCsrfAndCreatesSession() throws Exception {
        mvc.perform(post("/stage4/login").param("username","alice").param("password","123456"))
                .andExpect(status().isForbidden());
        var csrf = mvc.perform(get("/stage4/csrf")).andExpect(status().isOk()).andReturn();
        String token = JsonPath.read(csrf.getResponse().getContentAsString(), "$.token");
        var login = mvc.perform(post("/stage4/login").cookie(csrf.getResponse().getCookies())
                        .param("_csrf", token).param("username","alice").param("password","123456"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/stage4/me")).andReturn();
        mvc.perform(get("/stage4/me").session((org.springframework.mock.web.MockHttpSession)login.getRequest().getSession(false)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.username").value("alice"));
    }
}
