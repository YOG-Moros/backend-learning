package top.cch.redis.common;

import org.springframework.data.redis.core.script.DefaultRedisScript;

public final class RedisScripts {
    private RedisScripts() { }

    public static final DefaultRedisScript<Long> CONSUME_SMS = new DefaultRedisScript<>("""
            local code=redis.call('hget',KEYS[1],'code')
            if not code then return -1 end
            local status=redis.call('hget',KEYS[1],'status')
            if status and cjson.decode(status) == '1' then return -2 end
            if cjson.decode(code) ~= ARGV[1] then
              local failures=redis.call('incr',KEYS[2])
              if failures == 1 then redis.call('expire',KEYS[2],ARGV[3]) end
              if failures >= tonumber(ARGV[2]) then
                redis.call('del',KEYS[1],KEYS[2]); return -3
              end
              return 0
            end
            redis.call('hset',KEYS[1],'status',cjson.encode('1'))
            redis.call('del',KEYS[2]); return 1
            """, Long.class);

    public static final DefaultRedisScript<Long> UNLOCK = new DefaultRedisScript<>(
            "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end", Long.class);

    public static final DefaultRedisScript<Long> INCREMENT = new DefaultRedisScript<>(
            "local n=redis.call('incr',KEYS[1]); if redis.call('ttl',KEYS[1]) < 0 then redis.call('expire',KEYS[1],ARGV[1]) end; return n", Long.class);
}
