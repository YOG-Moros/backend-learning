package top.cch.security;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author cch
 * @date 2026/10/28
 * @description SecurityCaseTests 22 个验收用例：连通性 / 401 / 登录 / 403 越权 / 业务错误码 / 管理员接口。
 *              账号密码对齐种子数据：Student#2026 / Teacher#2026 / Admin#2026 / Guest#2026
 **/
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecurityCaseTests {

    @Autowired
    private MockMvc mockMvc;

    private static String studentToken;
    private static String student2Token;
    private static String teacherToken;
    private static String adminToken;

    private String login(String username, String password) throws Exception {
        String body = """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }

    // ---------- 公共与认证 ----------

    @Test
    @Order(1)
    void pingAnonymousShouldReturn200() throws Exception {
        mockMvc.perform(get("/public/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ok"));
    }

    @Test
    @Order(2)
    void noTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/tasks/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @Order(3)
    void studentLoginShouldReturnToken() throws Exception {
        studentToken = login("student01", "Student#2026");
        org.junit.jupiter.api.Assertions.assertFalse(studentToken.isBlank());
    }

    @Test
    @Order(4)
    void wrongPasswordShouldReturn30001() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"student01","password":"wrong-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(30001));
    }

    @Test
    @Order(5)
    void disabledAccountShouldReturn30002() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"guest","password":"Guest#2026"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(30002));
    }

    @Test
    @Order(6)
    void meShouldReturnIdentityWithAuthorities() throws Exception {
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("student01"))
                .andExpect(jsonPath("$.data.nickname").value("陈昶好"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles", hasItem("STUDENT")))
                .andExpect(jsonPath("$.data.authorities", hasItem("ROLE_STUDENT")))
                .andExpect(jsonPath("$.data.authorities", hasItem("task:submit")))
                .andExpect(jsonPath("$.data.authorities", hasItem("task:read")));
    }

    // ---------- 任务：列表 / 归属 / 越权 ----------

    @Test
    @Order(7)
    void teacherListAllShouldReturn28Tasks() throws Exception {
        teacherToken = login("teacher01", "Teacher#2026");
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(28)));
    }

    @Test
    @Order(8)
    void studentListAllShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @Order(9)
    void studentReadOthersTaskShouldReturn403() throws Exception {
        student2Token = login("student02", "Student#2026");
        // 任务 1 归属 student01（陈昶好），student02（李思远）无 task:review 权限点 → 越权 403
        mockMvc.perform(get("/api/tasks/1").header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @Order(10)
    void teacherReadAnyTaskShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/tasks/2").header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.owner").value("student02"))
                .andExpect(jsonPath("$.data.ownerNickname").value("李思远"))
                .andExpect(jsonPath("$.data.score").value(92));
    }

    @Test
    @Order(11)
    void studentMineShouldOnlyContainOwnTasks() throws Exception {
        mockMvc.perform(get("/api/tasks/mine").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].owner").value("student01"));
    }

    // ---------- 任务：提交与重复提交 ----------

    @Test
    @Order(12)
    void student10SubmitShouldSuccess() throws Exception {
        // student10（黄泽宇）在种子数据里没有待批改任务
        String token = login("student10", "Student#2026");
        mockMvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"实训六：登录后的练习","content":"完成 JWT 登录与权限点鉴权练习。"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.statusText").value("待批改"))
                .andExpect(jsonPath("$.data.owner").value("student10"))
                .andExpect(jsonPath("$.data.ownerNickname").value("黄泽宇"));
    }

    @Test
    @Order(13)
    void duplicateSubmitShouldReturn30011() throws Exception {
        String token = login("student10", "Student#2026");
        // 与上一步同名：同名任务待批改期间重复提交才会被 30011 拦
        mockMvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"实训六：登录后的练习","content":"同名任务还有待批改时的重复提交。"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(30011));
    }

    // ---------- 批改 ----------

    @Test
    @Order(14)
    void teacherReviewTask1ShouldSuccess() throws Exception {
        mockMvc.perform(post("/api/tasks/1/review").header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":88,"comment":"批改链路完整，理解到位。"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVIEWED"))
                .andExpect(jsonPath("$.data.statusText").value("已批改"))
                .andExpect(jsonPath("$.data.score").value(88));
    }

    @Test
    @Order(15)
    void duplicateReviewShouldReturn30012() throws Exception {
        mockMvc.perform(post("/api/tasks/1/review").header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":90,"comment":"重复批改"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(30012));
    }

    @Test
    @Order(16)
    void reviewNotExistTaskShouldReturn30010() throws Exception {
        mockMvc.perform(post("/api/tasks/999/review").header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":60}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(30010));
    }

    // ---------- 管理员接口 ----------

    @Test
    @Order(17)
    void adminListUsersShouldHidePassword() throws Exception {
        adminToken = login("admin", "Admin#2026");
        MvcResult result = mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(14)))
                .andExpect(jsonPath("$.data[0].username").value("admin"))
                .andExpect(jsonPath("$.data[0].roles", hasItem("ADMIN")))
                .andExpect(jsonPath("$.data[0].authorities", hasItem("user:write")))
                .andReturn();
        // 响应体里绝不能出现 password 字段
        org.junit.jupiter.api.Assertions.assertFalse(
                result.getResponse().getContentAsString().contains("password"));
    }

    @Test
    @Order(18)
    void adminCreateUserShouldSuccess() throws Exception {
        mockMvc.perform(post("/admin/users").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"newstu01","password":"NewStu#2026","nickname":"新同学","roles":["STUDENT"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("newstu01"))
                .andExpect(jsonPath("$.data.roles", hasItem("STUDENT")));
    }

    @Test
    @Order(19)
    void adminCreateDuplicateUserShouldReturn30004() throws Exception {
        mockMvc.perform(post("/admin/users").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"newstu01","password":"NewStu#2026","nickname":"重复账号","roles":["STUDENT"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(30004));
    }

    @Test
    @Order(20)
    void adminDeleteOtherUserShouldSuccess() throws Exception {
        mockMvc.perform(delete("/admin/users/newstu01").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.data", hasSize(14)));
    }

    @Test
    @Order(21)
    void adminDeleteSelfShouldReturn30005() throws Exception {
        mockMvc.perform(delete("/admin/users/admin").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(30005));
    }

    @Test
    @Order(22)
    void teacherAccessAdminShouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }
}
