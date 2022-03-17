package vn.zalopay.kafkascale.cache.redis;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import vn.zalopay.kafkascale.config.RedisConfig;
import vn.zalopay.kafkascale.util.GsonUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author anhnq7
 */
@Component
public class RedisCache extends BaseRedis<RedisConfig> implements DisposableBean {

    Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private final String ANTI_LOSS_CACHE_PREFIX = "trans_";
    private final String PROCESSED_CACHE_PREFIX = "processed_trans_";
    private RedissonClient redisson;

    public RedisCache(RedisConfig redisConf) {
        this.redisConfig = redisConf;

        Config config = new Config();
        Codec codec = new StringCodec();
        config.setCodec(codec);
        config = initRedissonConf(config);
        redisson = Redisson.create(config);
    }

    public <T> void cacheObject(String key, T value) {
        redisson.getBucket(key).set(value);
    }

    public <T> void cacheObjectWithExpire(String key, T value, long time, TimeUnit timeUnit) throws Exception {
        String jsonData = GsonUtil.toJsonString(value);
        redisson.getBucket(key).set(jsonData, time, timeUnit);
    }

    public <T> T getObjectFromCache(String key, Class<T> clazz) throws Exception {
        String jsonData = getString(key);
        return GsonUtil.fromJsonString(jsonData, clazz);
    }

    public <T> List<T> listObjects(Class<T> clazz) throws Exception {

        Iterable<String> iterable = redisson.getKeys().getKeysByPattern(ANTI_LOSS_CACHE_PREFIX + "*");

        List<T> cacheObjects = new ArrayList<>();
        for (Iterator<String> it = iterable.iterator(); it.hasNext(); ) {
            String key = it.next();
            T object = getObjectFromCache(key, clazz);
            cacheObjects.add(object);
        }

        return cacheObjects;
    }

    public String getString(String key) {
        RBucket<String> bucket = redisson.getBucket(key);
        String result = bucket.get();
        return result;
    }

    public void setString(String key, String value, long time, TimeUnit timeUnit) {
        RBucket<String> bucket = redisson.getBucket(key);
        bucket.set(value, time, timeUnit);
    }

    public boolean deleteString(String key) {
        RBucket<String> bucket = redisson.getBucket(key);
        return bucket.delete();
    }

    public boolean trySet(String key, String value, long time, TimeUnit timeUnit) {
        RBucket<String> bucket = redisson.getBucket(key);
        return bucket.trySet(value, time, timeUnit);
    }

    public Integer getInt(String key) {
        RBucket<Integer> bucket = redisson.getBucket(key);
        Integer obj = bucket.get();
        if (obj != null) {
            return obj;
        } else {
            return 0;
        }
    }

    public Long getLong(String key) {
        RBucket<Long> bucket = redisson.getBucket(key);
        Long obj = bucket.get();
        if (obj != null) {
            return obj;
        } else {
            return 0L;
        }
    }

    @Override
    public void destroy() {
        try {
            if (redisson != null) {
                redisson.shutdown();
            }
        } catch (Exception ex) {
            logger.error("Redisson shutdown error!", ex);
        }
    }

    public String getAntiLossCacheKey(long transId) {
        return ANTI_LOSS_CACHE_PREFIX + transId;
    }

    public String getProcessedCacheKey(long transId) {
        return PROCESSED_CACHE_PREFIX + transId;
    }

    public RedissonClient getRedisson() {
        return redisson;
    }
}
