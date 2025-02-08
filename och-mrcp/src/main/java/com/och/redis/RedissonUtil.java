package com.och.redis;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

public class RedissonUtil {

    public static RLock getLock(String lockKey) {
        return RedissonManager.getRedisson().getLock(lockKey);
    }

    public static boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        try {
            return getLock(lockKey).tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static void unlock(String lockKey) {
        RLock lock = getLock(lockKey);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public static void setValue(String key, Object value) {
        RBucket<Object> bucket = RedissonManager.getRedisson().getBucket(key);
        bucket.set(value);
    }

    public static <T> T getValue(String key) {
        RBucket<T> bucket = RedissonManager.getRedisson().getBucket(key);
        return bucket.get();
    }

    public static void delete(String key) {
        RedissonManager.getRedisson().getBucket(key).delete();
    }

    public static void setValue(String key, String value, long expireTime, TimeUnit timeUnit) {
        RBucket<String> bucket = RedissonManager.getRedisson().getBucket(key);
        bucket.set(value, expireTime, timeUnit);
    }

    public static Boolean isExists(String key) {
        RBucket<String> bucket = RedissonManager.getRedisson().getBucket(key);
        return bucket.isExists();
    }
}
