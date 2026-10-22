package top.cch.mybatis;

import com.jayway.jsonpath.JsonPath;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import top.cch.mybatis.entity.User;
import top.cch.mybatis.service.UserService;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /users 接口逐个验证，Controller 直接返回实体（无统一响应体包装）
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserApiTest {

    @Resource
    private MockMvc mockMvc;
    @Resource
    private UserService userService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTable() {
        jdbcTemplate.execute("DELETE FROM t_user");
    }

    private User seedUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setAge(20);
        userService.createUser(user);
        return user;
    }

    private User seedUser(String username, Integer gender) {
        User user = new User();
        user.setUsername(username);
        user.setAge(20);
        user.setGender(gender);
        userService.createUser(user);
        return user;
    }

    private long parseId(MvcResult result) throws Exception {
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    /* -------------------- 增 -------------------- */

    @Test
    void createReturnsAuditFieldsAndHidesPassword() throws Exception {
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"zhouba\", \"age\": 28, \"tags\": [\"Java\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createTime").isNotEmpty())
                .andExpect(jsonPath("$.updateTime").isNotEmpty())
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.deleted").value(0))
                .andExpect(jsonPath("$.tags[0]").value("Java"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();
        long id = parseId(result);
        assertEquals(28, userService.getUser(id).getAge());
    }

    @Test
    void createWithInvalidAgeReturns500AndRollsBack() throws Exception {
        // 本模块没有全局异常处理器，未处理异常在 MockMvc 里直接抛出（真实容器里是 500）
        assertThrows(jakarta.servlet.ServletException.class, () -> mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"bad\", \"age\": 200}")));
        assertEquals(0, userService.count(), "事务应回滚，库里不落数据");
    }

    @Test
    void createBatchReturnsCount() throws Exception {
        mockMvc.perform(post("/users/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"username\": \"x1\"}, {\"username\": \"x2\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
        assertEquals(2, userService.count());
    }

    /* -------------------- 查 -------------------- */

    @Test
    void getByIdReturnsFullUserWithTags() throws Exception {
        User tagged = new User();
        tagged.setUsername("detail");
        tagged.setAge(20);
        tagged.setTags(java.util.List.of("Java", "SQL"));
        userService.createUser(tagged);

        mockMvc.perform(get("/users/{id}", tagged.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("detail"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getByIdMissingReturnsEmptyBody() throws Exception {
        mockMvc.perform(get("/users/{id}", 99999))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void listFiltersByQueryParams() throws Exception {
        seedUser("m1", 1);
        seedUser("m2", 1);
        seedUser("f1", 2);

        mockMvc.perform(get("/users").param("gender", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("m2"));
    }

    @Test
    void pageReturnsPagedPayload() throws Exception {
        for (int i = 1; i <= 5; i++) {
            seedUser("p" + i);
        }

        mockMvc.perform(get("/users/page")
                        .param("current", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(3)))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.current").value(1))
                .andExpect(jsonPath("$.pages").value(2));
    }

    @Test
    void keywordSearchHitsNickname() throws Exception {
        User user = seedUser("kw");
        jdbcTemplate.update("UPDATE t_user SET nickname = 'ZhangSan' WHERE id = ?", user.getId());

        mockMvc.perform(get("/users/keyword").param("keyword", "Zhang"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("kw"));
    }

    @Test
    void topOrdersByPointsDesc() throws Exception {
        jdbcTemplate.execute("DELETE FROM t_user");
        for (int i = 1; i <= 3; i++) {
            User user = new User();
            user.setUsername("t" + i);
            user.setAge(20);
            user.setPoints(i * 10);
            userService.createUser(user);
        }

        mockMvc.perform(get("/users/top").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("t3"))
                .andExpect(jsonPath("$[1].username").value("t2"));
    }

    @Test
    void usernamesReturnsStringArray() throws Exception {
        seedUser("u1");
        seedUser("u2");

        mockMvc.perform(get("/users/usernames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value("u1"));
    }

    @Test
    void tagFilterQueriesJsonColumn() throws Exception {
        jdbcTemplate.execute("DELETE FROM t_user");
        User user = new User();
        user.setUsername("tagged");
        user.setAge(20);
        user.setTags(java.util.List.of("Java", "篮球"));
        userService.createUser(user);
        seedUser("untagged");

        mockMvc.perform(get("/users/tag").param("tag", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("tagged"));
    }

    @Test
    void existsAndCountEndpoints() throws Exception {
        seedUser("u1");
        seedUser("u2");

        mockMvc.perform(get("/users/exists").param("username", "u1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        mockMvc.perform(get("/users/exists").param("username", "nope"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
        mockMvc.perform(get("/users/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void statisticsEndpointReturnsAggregates() throws Exception {
        seedUser("s1");
        seedUser("s2");

        mockMvc.perform(get("/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.genderCount").isArray());
    }

    @Test
    void updateEmailByUsernameEndpoint() throws Exception {
        seedUser("mailer");

        mockMvc.perform(put("/users/email")
                        .param("username", "mailer")
                        .param("email", "apifox@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    /* -------------------- 8.2 练习 2：@JsonFormat 输入输出格式 -------------------- */

    @Test
    void jsonFormatAcceptsAndReturnsSqlStyleDateTime() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"fmt\", \"birthday\": \"2000-01-02\", "
                                + "\"workTime\": \"09:30:00\", \"lastLoginTime\": \"2026-10-01 10:20:30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthday").value("2000-01-02"))
                .andExpect(jsonPath("$.workTime").value("09:30:00"))
                .andExpect(jsonPath("$.lastLoginTime").value("2026-10-01 10:20:30"))
                .andExpect(jsonPath("$.createTime").isNotEmpty());
    }

    /* -------------------- 改 -------------------- */

    @Test
    void updateRefreshesFieldsAndUpdateTime() throws Exception {
        User user = seedUser("upd");

        mockMvc.perform(put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\": \"api-updated\", \"age\": 23}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.nickname").value("api-updated"))
                .andExpect(jsonPath("$.age").value(23))
                .andExpect(jsonPath("$.updateTime").isNotEmpty());

        User dbUser = userService.getUser(user.getId());
        assertEquals("api-updated", dbUser.getNickname());
        assertEquals(23, dbUser.getAge());
    }

    @Test
    void updateStatusEndpointFlipsStatus() throws Exception {
        User user = seedUser("flip");

        mockMvc.perform(put("/users/{id}/status", user.getId()).param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        assertEquals(0, userService.getUser(user.getId()).getStatus());
    }

    @Test
    void rechargeAddsBalanceAndBumpsVersion() throws Exception {
        jdbcTemplate.execute("DELETE FROM t_user");
        User user = new User();
        user.setUsername("rich");
        user.setAge(20);
        user.setBalance(new BigDecimal("100.00"));
        userService.createUser(user);

        mockMvc.perform(post("/users/{id}/recharge", user.getId()).param("amount", "100.25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.25))
                .andExpect(jsonPath("$.version").value(1));
    }

    /* -------------------- 删 -------------------- */

    @Test
    void deleteThenGetReturnsEmptyBody() throws Exception {
        User user = seedUser("del");

        mockMvc.perform(delete("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        mockMvc.perform(get("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void deleteBatchEndpoint() throws Exception {
        User a = seedUser("b1");
        User b = seedUser("b2");

        mockMvc.perform(delete("/users/batch")
                        .param("ids", a.getId() + "," + b.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        assertEquals(0, userService.count());
    }

    @Test
    void deleteByStatusEndpoint() throws Exception {
        User active = seedUser("ok");
        User disabled = seedUser("off");
        userService.modifyUserStatus(disabled.getId(), 0);

        mockMvc.perform(delete("/users/status/{status}", 0))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        assertNotNull(userService.getUser(active.getId()));
        assertEquals(1, userService.count(), "只删 status=0 的那条");
    }
}
