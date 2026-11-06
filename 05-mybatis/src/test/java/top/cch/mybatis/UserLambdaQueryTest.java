package top.cch.mybatis;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import top.cch.mybatis.dto.UserQuery;
import top.cch.mybatis.entity.User;
import top.cch.mybatis.service.UserService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lambda 条件构造器：动态条件、模糊、区间、排序、单列、JSON 列、分组统计
 */
@SpringBootTest
class UserLambdaQueryTest {

    @Resource
    private UserService userService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTable() {
        jdbcTemplate.execute("DELETE FROM t_user");
    }

    private User seed(String username, String nickname, Integer gender, Integer age,
                      String memberLevel, boolean vip, Integer points, List<String> tags, Integer status) {
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setGender(gender);
        user.setAge(age);
        user.setMemberLevel(memberLevel);
        user.setVip(vip);
        user.setPoints(points);
        user.setTags(tags);
        user.setStatus(status);
        userService.createUser(user);
        return user;
    }

    @Test
    void listByConditionFiltersByGenderAndOrdersByIdDesc() {
        User u1 = seed("u1", "甲", 1, 20, "BRONZE", false, 0, null, 1);
        seed("u2", "乙", 2, 22, "SILVER", true, 0, null, 1);
        User u3 = seed("u3", "丙", 1, 24, "GOLD", false, 0, null, 1);

        UserQuery query = new UserQuery();
        query.setGender(1);
        List<User> users = userService.listByCondition(query);

        assertEquals(2, users.size());
        assertEquals(u3.getId(), users.get(0).getId(), "应按 id 倒序");
        assertEquals(u1.getId(), users.get(1).getId());
    }

    @Test
    void listByConditionFiltersByMemberLevelAndVip() {
        seed("m1", "甲", 1, 20, "GOLD", true, 0, null, 1);
        seed("m2", "乙", 1, 21, "GOLD", false, 0, null, 1);
        seed("m3", "丙", 1, 22, "BRONZE", true, 0, null, 1);

        UserQuery query = new UserQuery();
        query.setMemberLevel("GOLD");
        query.setVip(true);
        List<User> users = userService.listByCondition(query);

        assertEquals(1, users.size());
        assertEquals("m1", users.get(0).getUsername());
    }

    @Test
    void listByConditionFiltersByStatus() {
        seed("st1", "甲", 1, 20, "BRONZE", false, 0, null, 1);
        seed("st0", "乙", 1, 21, "BRONZE", false, 0, null, 0);

        UserQuery query = new UserQuery();
        query.setStatus(0);
        List<User> users = userService.listByCondition(query);

        assertEquals(1, users.size());
        assertEquals("st0", users.get(0).getUsername());
    }

    @Test
    void listByConditionFiltersByAgeRange() {
        seed("a1", "甲", 1, 18, "BRONZE", false, 0, null, 1);
        seed("a2", "乙", 1, 25, "BRONZE", false, 0, null, 1);
        seed("a3", "丙", 1, 30, "BRONZE", false, 0, null, 1);

        UserQuery query = new UserQuery();
        query.setMinAge(20);
        query.setMaxAge(28);
        List<User> users = userService.listByCondition(query);

        assertEquals(1, users.size());
        assertEquals("a2", users.get(0).getUsername());
    }

    @Test
    void listByConditionKeywordMatchesUsernameOrNickname() {
        seed("kw-user", "甲", 1, 20, "BRONZE", false, 0, null, 1);
        seed("other", "管理员小明", 1, 21, "BRONZE", false, 0, null, 1);
        seed("third", "丙", 1, 22, "BRONZE", false, 0, null, 1);

        UserQuery query = new UserQuery();
        query.setKeyword("kw");
        assertEquals(1, userService.listByCondition(query).size(), "keyword 命中 username");

        query.setKeyword("管理员");
        assertEquals(1, userService.listByCondition(query).size(), "keyword 命中 nickname");
    }

    @Test
    void listByConditionCombinesConditions() {
        seed("c1", "甲", 1, 20, "BRONZE", false, 0, null, 1);
        seed("c2", "乙", 1, 25, "BRONZE", false, 0, null, 1);
        seed("c3", "丙", 2, 25, "BRONZE", false, 0, null, 1);

        UserQuery query = new UserQuery();
        query.setGender(1);
        query.setMinAge(24);
        List<User> users = userService.listByCondition(query);

        assertEquals(1, users.size());
        assertEquals("c2", users.get(0).getUsername());
    }

    @Test
    void listByConditionWithEmptyQueryReturnsAll() {
        seed("e1", "甲", 1, 20, "BRONZE", false, 0, null, 1);
        seed("e2", "乙", 2, 21, "BRONZE", false, 0, null, 1);

        assertEquals(2, userService.listByCondition(new UserQuery()).size());
        assertEquals(2, userService.listByCondition(null).size());
    }

    @Test
    void pageByConditionPaginatesWithFilter() {
        for (int i = 1; i <= 5; i++) {
            seed("p" + i, "用户" + i, 1, 20, "BRONZE", false, 0, null, 1);
        }
        UserQuery query = new UserQuery();
        query.setGender(1);
        var page = userService.pageByCondition(query, 2, 2);

        assertEquals(5, page.getTotal());
        assertEquals(3, page.getPages());
        assertEquals(2, page.getRecords().size());
        assertEquals("p3", page.getRecords().get(0).getUsername(), "第二页第一条");
    }

    @Test
    void searchByKeywordOrdersByIdAsc() {
        User u1 = seed("s1", "张三", 1, 20, "BRONZE", false, 0, null, 1);
        User u2 = seed("s2", "小张", 1, 21, "BRONZE", false, 0, null, 1);

        List<User> users = userService.searchByKeyword("张");
        assertEquals(2, users.size());
        assertEquals(u1.getId(), users.get(0).getId());
        assertEquals(u2.getId(), users.get(1).getId());
    }

    @Test
    void listByAgeRangeOrdersByAgeAsc() {
        seed("age30", "甲", 1, 30, "BRONZE", false, 0, null, 1);
        seed("age20", "乙", 1, 20, "BRONZE", false, 0, null, 1);
        seed("age25", "丙", 1, 25, "BRONZE", false, 0, null, 1);

        List<User> users = userService.listByAgeRange(18, 35);
        assertEquals(3, users.size());
        assertEquals("age20", users.get(0).getUsername());
        assertEquals("age25", users.get(1).getUsername());
        assertEquals("age30", users.get(2).getUsername());
    }

    @Test
    void listTopReturnsHighestPointsLimited() {
        seed("t40", "甲", 1, 20, "BRONZE", false, 40, null, 1);
        seed("t30", "乙", 1, 21, "BRONZE", false, 30, null, 1);
        seed("t20", "丙", 1, 22, "BRONZE", false, 20, null, 1);

        List<User> users = userService.listTop(2);
        assertEquals(2, users.size());
        assertEquals("t40", users.get(0).getUsername());
        assertEquals("t30", users.get(1).getUsername());
    }

    @Test
    void listUsernamesReturnsSingleColumn() {
        seed("n1", "甲", 1, 20, "BRONZE", false, 0, null, 1);
        seed("n2", "乙", 1, 21, "BRONZE", false, 0, null, 1);

        assertEquals(List.of("n1", "n2"), userService.listUsernames());
    }

    @Test
    void listByTagQueriesJsonColumn() {
        seed("j1", "甲", 1, 20, "BRONZE", false, 0, List.of("Java", "篮球"), 1);
        seed("j2", "乙", 1, 21, "BRONZE", false, 0, List.of("音乐"), 1);

        List<User> users = userService.listByTag("Java");
        assertEquals(1, users.size());
        assertEquals("j1", users.get(0).getUsername());
        assertEquals(List.of("Java", "篮球"), users.get(0).getTags(), "tags 应经 typeHandler 反序列化回来");
    }

    @Test
    void countByGenderAndExistsUsername() {
        seed("g1", "甲", 1, 20, "BRONZE", false, 0, null, 1);
        seed("g2", "乙", 1, 21, "BRONZE", false, 0, null, 1);
        seed("g3", "丙", 2, 22, "BRONZE", false, 0, null, 1);

        assertEquals(2, userService.countByGender(1));
        assertEquals(userService.count(), userService.countByGender(null));
        assertTrue(userService.existsUsername("g1"));
        assertFalse(userService.existsUsername("nope"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void statisticsAggregatesAndGroups() {
        seed("x1", "甲", 1, 30, "BRONZE", false, 10, null, 1);
        seed("x2", "乙", 2, 20, "SILVER", true, 20, null, 1);

        Map<String, Object> stats = userService.statistics();
        assertEquals(2L, ((Number) stats.get("totalCount")).longValue());
        assertEquals(30L, ((Number) stats.get("maxAge")).longValue());

        List<Map<String, Object>> genderCount = (List<Map<String, Object>>) stats.get("genderCount");
        assertEquals(2, genderCount.size());
    }
}
