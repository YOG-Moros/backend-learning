package top.cch.redis;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import top.cch.redis.common.RedisScripts;
import top.cch.redis.service.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 独立 Redis db 14；不执行 FLUSHDB，MySQL 写入在每条测试后回滚。 */
@SpringBootTest(properties = {"spring.data.redis.database=14", "cch.sms.mock-enabled=true"})
@AutoConfigureMockMvc
@Transactional
class RedisChapterIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired StringRedisTemplate redis;
    @Autowired GoodsCacheService manual;
    @Autowired GoodsSpringCacheService annotated;
    @Autowired StockService stock;
    @Autowired top.cch.redis.common.RedisCache cache;
    private final String prefix = "test:chapter:";

    @BeforeEach @AfterEach void clearOwnKeys() {
        for (String pattern : List.of("test:chapter:*", "goods:detail:*", "cache:goods::*", "limit:access:chapter-test:*", "rank:goods:sales")) {
            Set<String> keys = redis.keys(pattern);
            if (keys != null && !keys.isEmpty()) redis.delete(keys);
        }
    }
    @Test void manualCacheAndNullMarkerHaveFiniteTtl() {
        assertThat(manual.getById(1L).getSource()).isEqualTo("db");
        assertThat(manual.getById(1L).getSource()).isEqualTo("cache");
        assertThat(redis.getExpire("goods:detail:1")).isPositive();
        manual.getById(999999L);
        assertThat(manual.getById(999999L).getSource()).isEqualTo("cache-empty");
        assertThat(redis.getExpire("goods:detail:999999")).isLessThan(redis.getExpire("goods:detail:1"));
    }
    @Test void annotatedUpdateInvalidatesList() {
        annotated.listAll();
        annotated.updatePrice(1L, new BigDecimal("42.00"));
        assertThat(annotated.getById(1L).getPrice()).isEqualByComparingTo("42");
        assertThat(annotated.listAll().stream().filter(g -> g.getId()==1L).findFirst().orElseThrow().getPrice()).isEqualByComparingTo("42");
        annotated.evictAll();
        assertThat(redis.keys("cache:goods::*")).isEmpty();
    }
    @Test void mutexRebuildDoesNotLeaveLock() {
        assertThat(manual.getByIdWithMutex(2L).getSource()).isEqualTo("db");
        assertThat(manual.getByIdWithMutex(2L).getSource()).isEqualTo("cache");
        assertThat(redis.hasKey("lock:goods:rebuild:2")).isFalse();
    }
    @Test void expiredOwnerCannotDeleteNewOwnersLock() {
        String key=prefix+"lock";
        redis.opsForValue().set(key,"new-owner",Duration.ofSeconds(5));
        assertThat(redis.execute(RedisScripts.UNLOCK,List.of(key),"old-owner")).isZero();
        assertThat(redis.opsForValue().get(key)).isEqualTo("new-owner");
        assertThat(redis.execute(RedisScripts.UNLOCK,List.of(key),"new-owner")).isEqualTo(1);
    }
    @Test void fixedWindowRejectsSixthRequest() throws Exception {
        for (int i=0;i<5;i++) mvc.perform(get("/limit/access").param("user","chapter-test"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.allowed").value(true));
        mvc.perform(get("/limit/access").param("user","chapter-test"))
                .andExpect(jsonPath("$.allowed").value(false)).andExpect(jsonPath("$.remaining").value(0))
                .andExpect(jsonPath("$.ttlSeconds",greaterThan(0)));
    }
    @Test void hashExcludesControlParametersAndPreservesOtherFields() throws Exception {
        mvc.perform(post("/redis/hash").param("key",prefix+"hash").param("name","张三").param("age","20"))
                .andExpect(jsonPath("$.key").doesNotExist()).andExpect(jsonPath("$.name").value("张三"));
        mvc.perform(post("/redis/hash").param("key",prefix+"hash").param("age","21").param("ttlSeconds","30"))
                .andExpect(jsonPath("$.name").value("张三")).andExpect(jsonPath("$.age").value("21"))
                .andExpect(jsonPath("$.ttlSeconds").doesNotExist());
        assertThat(redis.getExpire(prefix+"hash")).isPositive();
    }
    @Test void listOrderAndSetIntersection() throws Exception {
        mvc.perform(post("/redis/list").param("key",prefix+"list").param("values","a,b,a"))
                .andExpect(jsonPath("$",contains("a","b","a")));
        mvc.perform(post("/redis/set").param("key",prefix+"s1").param("values","java,redis,java"))
                .andExpect(jsonPath("$",hasSize(2)));
        mvc.perform(post("/redis/set").param("key",prefix+"s2").param("values","java,mysql"));
        mvc.perform(get("/redis/set/intersect").param("key1",prefix+"s1").param("key2",prefix+"s2"))
                .andExpect(jsonPath("$",contains("java")));
    }
    @Test void countersAndSortedSets() throws Exception {
        mvc.perform(post("/redis/counter/incr").param("key",prefix+"counter")).andExpect(content().string("1"));
        mvc.perform(post("/redis/counter/incr").param("key",prefix+"counter")).andExpect(content().string("2"));
        mvc.perform(post("/redis/zset").param("key",prefix+"z").param("member","alice").param("score","10"));
        mvc.perform(post("/redis/zset").param("key",prefix+"z").param("member","bob").param("score","20"))
                .andExpect(jsonPath("$[0].value").value("bob"));
        assertThat(redis.getExpire(prefix+"counter")).isPositive();
    }
    @Test void lockedStockCannotBecomeNegativeAndQuantityMustBePositive() {
        stock.resetStock(1L,1);
        assertThat(stock.deductWithLock(1L,1)).isTrue();
        assertThat(stock.deductWithLock(1L,1)).isFalse();
        assertThat(stock.getStock(1L)).isZero();
        assertThatThrownBy(()->stock.deductWithLock(1L,-1)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void rankingsAreOrderedAndExpire() throws Exception {
        mvc.perform(post("/rank/warmup")).andExpect(status().isOk());
        mvc.perform(post("/rank/increase").param("id","3").param("delta","100000"));
        mvc.perform(get("/rank/top").param("top","1")).andExpect(jsonPath("$[0].value").value("3"));
        assertThat(redis.getExpire("rank:goods:sales")).isPositive();
    }
    @Test void newDemoEndpointsRemainAnonymousButUserDataDoesNot() throws Exception {
        mvc.perform(get("/cache/goods/manual/1")).andExpect(jsonPath("$.source").value("db"));
        mvc.perform(get("/user/me")).andExpect(jsonPath("$.code").value(40100));
    }

    @Test void smsCanOnlyBeConsumedOnceAcrossThreads() throws Exception {
        String key = prefix + "sms";
        cache.hMSet(key, Map.of("code", "0123", "status", "0"), 60);
        var start = new java.util.concurrent.CountDownLatch(1);
        try (var pool = java.util.concurrent.Executors.newFixedThreadPool(8)) {
            var results = new ArrayList<java.util.concurrent.Future<Long>>();
            for (int i=0;i<8;i++) results.add(pool.submit(() -> {
                start.await(); return cache.consumeSms(key, key+":fail", "0123", 5, 60);
            }));
            start.countDown();
            int successful = 0;
            for (var result : results) if (result.get(5, java.util.concurrent.TimeUnit.SECONDS) == 1) successful++;
            assertThat(successful).isEqualTo(1);
        }
        assertThat(cache.hGet(key, "status")).isEqualTo("1");
        assertThat(redis.getExpire(key)).isPositive();
    }

    @Test void smsWrongAttemptsInvalidateCodeWithoutExtendingTtl() {
        String key = prefix + "sms-fail";
        cache.hMSet(key, Map.of("code", "0123", "status", "0"), 60);
        assertThat(cache.consumeSms(key, key+":fail", "wrong", 2, 60)).isZero();
        assertThat(cache.consumeSms(key, key+":fail", "wrong", 2, 60)).isEqualTo(-3);
        assertThat(cache.consumeSms(key, key+":fail", "0123", 2, 60)).isEqualTo(-1);
    }
}
