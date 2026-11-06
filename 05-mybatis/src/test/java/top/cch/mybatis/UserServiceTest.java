package top.cch.mybatis;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import top.cch.mybatis.entity.User;
import top.cch.mybatis.service.UserService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service 层：增删改查、默认值、自动填充、逻辑删除、乐观锁、事务
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTable() {
        // 物理清空：绕开逻辑删除，也避免 uk_username 唯一键冲突
        jdbcTemplate.execute("DELETE FROM t_user");
    }

    private User newUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setAge(20);
        return user;
    }

    @Test
    void createUserFillsDefaultsAndAuditFields() {
        User user = newUser("defaults");
        assertTrue(userService.createUser(user));
        assertNotNull(user.getId());

        User dbUser = userService.getUser(user.getId());
        assertEquals("defaults", dbUser.getUsername());
        assertEquals(64, dbUser.getPassword().length(), "密码应存 SHA-256 摘要");
        assertEquals(0, dbUser.getGender());
        assertEquals("BRONZE", dbUser.getMemberLevel());
        assertEquals(0, dbUser.getPoints());
        assertEquals(0, BigDecimal.ZERO.compareTo(dbUser.getBalance()));
        assertFalse(dbUser.getVip());
        assertEquals(1, dbUser.getStatus());
        assertEquals(0, dbUser.getDeleted());
        assertEquals(0, dbUser.getVersion());
        assertNotNull(dbUser.getCreateTime());
        assertNotNull(dbUser.getUpdateTime());
    }

    @Test
    void createUserRollsBackOnInvalidAge() {
        User user = newUser("rollback");
        user.setAge(200);
        assertThrows(RuntimeException.class, () -> userService.createUser(user));
        assertEquals(0, userService.count(), "写库后抛异常应整体回滚");
    }

    @Test
    void createUserRejectsBlankUsername() {
        User user = newUser("  ");
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
    }

    @Test
    void createUserBatchSavesAll() {
        List<User> users = List.of(newUser("b1"), newUser("b2"), newUser("b3"));
        assertTrue(userService.createUserBatch(users));
        assertEquals(3, userService.count());
    }

    @Test
    void removeUserIsLogicalNotPhysical() {
        User user = newUser("logic-del");
        userService.createUser(user);

        assertTrue(userService.removeUser(user.getId()));
        assertNull(userService.getUser(user.getId()), "逻辑删除后应查不到");

        Integer deleted = jdbcTemplate.queryForObject(
                "SELECT deleted FROM t_user WHERE id = ?", Integer.class, user.getId());
        assertEquals(1, deleted, "物理行还在，只是 deleted=1");
    }

    @Test
    void removeUserBatchDeletesAll() {
        User a = newUser("d1");
        User b = newUser("d2");
        userService.createUserBatch(List.of(a, b));

        assertTrue(userService.removeUserBatch(List.of(a.getId(), b.getId())));
        assertEquals(0, userService.count());
    }

    @Test
    void removeUserByStatusOnlyHitsMatching() {
        User active = newUser("s1");
        active.setStatus(1);
        User disabled = newUser("s0");
        disabled.setStatus(0);
        userService.createUserBatch(List.of(active, disabled));

        assertTrue(userService.removeUserByStatus(0));
        assertNotNull(userService.getUser(active.getId()));
        assertNull(userService.getUser(disabled.getId()));
    }

    @Test
    void modifyUserRequiresId() {
        assertThrows(IllegalArgumentException.class, () -> userService.modifyUser(newUser("noid")));
    }

    @Test
    void modifyUserRejectsUnknownId() {
        User user = newUser("ghost");
        user.setId(99999L);
        assertThrows(IllegalArgumentException.class, () -> userService.modifyUser(user));
    }

    @Test
    void modifyUserUpdatesNonNullFieldsOnly() {
        User user = newUser("modify");
        user.setNickname("旧昵称");
        userService.createUser(user);

        User change = new User();
        change.setId(user.getId());
        change.setNickname("新昵称");
        assertTrue(userService.modifyUser(change));

        User dbUser = userService.getUser(user.getId());
        assertEquals("新昵称", dbUser.getNickname());
        assertEquals(20, dbUser.getAge(), "age 为 null 时不应被更新");
    }

    @Test
    void modifyUserStatusFlipsStatus() {
        User user = newUser("status");
        userService.createUser(user);

        assertTrue(userService.modifyUserStatus(user.getId(), 0));
        assertEquals(0, userService.getUser(user.getId()).getStatus());
    }

    @Test
    void rechargeAddsBalanceAndBumpsVersion() {
        User user = newUser("recharge");
        user.setBalance(new BigDecimal("100.00"));
        userService.createUser(user);

        assertTrue(userService.recharge(user.getId(), new BigDecimal("50.50")));
        User dbUser = userService.getUser(user.getId());
        assertEquals(new BigDecimal("150.50"), dbUser.getBalance());
        assertEquals(1, dbUser.getVersion(), "充值更新应把版本号加一");
    }

    @Test
    void rechargeRejectsInvalidAmountAndUnknownUser() {
        User user = newUser("recharge2");
        userService.createUser(user);
        assertThrows(IllegalArgumentException.class,
                () -> userService.recharge(user.getId(), BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> userService.recharge(99999L, BigDecimal.TEN));
    }

    @Test
    void optimisticLockRejectsStaleEntity() {
        User user = newUser("version");
        userService.createUser(user);

        User first = userService.getUser(user.getId());
        User stale = userService.getUser(user.getId());

        first.setNickname("第一次改");
        assertTrue(userService.modifyUser(first), "最新版本应更新成功");

        stale.setNickname("第二次改");
        assertFalse(userService.modifyUser(stale), "旧版本应被乐观锁拦下");

        User dbUser = userService.getUser(user.getId());
        assertEquals("第一次改", dbUser.getNickname());
        assertEquals(1, dbUser.getVersion());
    }

    @Test
    void pageUsersReturnsPagedData() {
        for (int i = 1; i <= 5; i++) {
            userService.createUser(newUser("page" + i));
        }
        var page = userService.pageUsers(1, 2);
        assertEquals(2, page.getRecords().size());
        assertEquals(5, page.getTotal());
        assertEquals(3, page.getPages());
    }

    @Test
    void modifyEmailByUsernameUpdatesWithoutId() {
        User user = newUser("mail");
        userService.createUser(user);

        assertTrue(userService.modifyEmailByUsername("mail", "new@example.com"),
                "LambdaUpdateWrapper 按 username 定位，不需要主键");
        assertEquals("new@example.com", userService.getUser(user.getId()).getEmail());

        assertFalse(userService.modifyEmailByUsername("nobody", "x@example.com"),
                "用户不存在时影响 0 行，应返回 false");
    }
}
