package com.litepan.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: Redis存取
 * @date 2024/7/19 9:54
 */
@Component
@Slf4j
public class RedisUtils<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;

    /**
     * 删除缓存
     *
     * @param key 可变长参数
     */
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     * 从缓存中获取值
     *
     * @param key 键
     * @return value 值
     */
    public V get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true：存入成功；false：存入失败
     */
    public boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置redis缓存失败，key:{}，value:{}", key, value, e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  过期时间（秒），若time小于等于0，则表示设置无限期
     * @return true：存入成功；false：存入失败
     */
    public boolean setEx(String key, V value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("设置redis缓存失败，key:{}，value:{}", key, value, e);
            return false;
        }
    }

}
