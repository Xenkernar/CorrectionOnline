package com.xenkernar.pdlrms.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Set;

@Repository
public class StringSetRepository {
    @Autowired
    private RedisTemplate redisTemplate;

    public void addValue(String key, String value) {
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(key);
        setOps.add(value);
    }

    public void removeValue(String key, String value) {
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(key);
        setOps.remove(value);
    }

    public void removeValues(String key) {
        redisTemplate.delete(key);
    }

    public Set<String> getValues(String key) {
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(key);
        return setOps.members();
    }

    //清除所有数据
    public void clearAll(){
        RedisConnection redisConnection = Objects.requireNonNull(this.redisTemplate.getConnectionFactory()).getConnection();
        RedisSerializer<String> redisSerializer = this.redisTemplate.getKeySerializer();
        DefaultStringRedisConnection defaultStringRedisConnection = new DefaultStringRedisConnection(redisConnection,  redisSerializer);
        defaultStringRedisConnection.flushAll();
    }
}
