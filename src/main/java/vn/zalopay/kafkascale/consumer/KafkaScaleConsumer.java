package vn.zalopay.kafkascale.consumer;

import jodd.util.StringUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import vn.zalopay.kafkascale.cache.redis.RedisCache;
import vn.zalopay.kafkascale.entity.KafkaMessage;
import vn.zalopay.kafkascale.entity.TransEntity;
import vn.zalopay.kafkascale.entity.TransEntityRedisWrapper;
import vn.zalopay.kafkascale.entity.TransProcessedEntity;
import vn.zalopay.kafkascale.threadpool.KafkaScaleThreadPool;
import vn.zalopay.kafkascale.util.GsonUtil;

import java.util.concurrent.TimeUnit;

@Component
public class KafkaScaleConsumer {

    Logger rootLogger = LoggerFactory.getLogger(KafkaScaleConsumer.class);
    Logger logger = LoggerFactory.getLogger("consumer");

    private final KafkaScaleThreadPool threadPool;
    private final RedisCache redisCache;

    public KafkaScaleConsumer(KafkaScaleThreadPool threadPool, RedisCache redisCache) {
        this.threadPool = threadPool;
        this.redisCache = redisCache;
    }

    @KafkaListener(
            topics = "${kafka.topic.name}",
            groupId = "${kafka.topic.groupId}",
            concurrency = "${kafka.topic.concurrency}")
    public void consume(String kafkaMessageStr, Acknowledgment acknowledgment) {
        try {
            KafkaMessage kafkaMessage = GsonUtil.fromJsonString(kafkaMessageStr, KafkaMessage.class);
            TransEntity transEntity = GsonUtil.fromJsonString(kafkaMessage.getData(), TransEntity.class);
            handleWithThreadPool(transEntity, acknowledgment);
            //doHandle(transEntity, acknowledgment);
        } catch (Exception exception) {
            rootLogger.error("consume " + kafkaMessageStr, exception);
        }
    }

    private void handleWithThreadPool(TransEntity transEntity, Acknowledgment acknowledgment) {
        threadPool.run(new Runnable() {
            @Override
            public void run() {
                try {
                    String cacheKey = redisCache.getAntiLossCacheKey(transEntity.getTransId());
                    TransEntityRedisWrapper transEntityRedisWrapper = redisCache.getObjectFromCache(cacheKey, TransEntityRedisWrapper.class);
                    if (transEntityRedisWrapper == null) {
                        transEntityRedisWrapper = new TransEntityRedisWrapper();
                        transEntityRedisWrapper.setTransEntity(transEntity);
                    }
                    transEntityRedisWrapper.setUpdateTime(System.currentTimeMillis());
                    transEntityRedisWrapper.setRetryTime(transEntityRedisWrapper.getRetryTime() + 1);
                    redisCache.cacheObject(cacheKey, GsonUtil.toJsonString(transEntityRedisWrapper));
                } catch (Exception e) {
                    rootLogger.error("Exception handleWithThreadPool " + transEntity.getTransId(), e);
                }
                doHandle(transEntity, acknowledgment);
            }
        });
    }

    private void doHandle(TransEntity transEntity, Acknowledgment acknowledgment) {
        acknowledgment.acknowledge();

        try {
            Thread.sleep(100l);
        } catch (InterruptedException e) {
            rootLogger.error("Exception doHandle " + transEntity.getTransId(), e);
        }

        // Check duplicate
        RedissonClient redissonClient = redisCache.getRedisson();
        RLock lock = redissonClient.getLock(transEntity.getTransId() + "");

        lock.lock();
        try {
            String processedCacheKey = redisCache.getProcessedCacheKey(transEntity.getTransId());
            try {
                TransProcessedEntity transProcessedEntity = redisCache.getObjectFromCache(processedCacheKey, TransProcessedEntity.class);
                if (transProcessedEntity != null) {
                    return;
                }
            } catch (Exception e) {
                rootLogger.error("Exception transProcessedEntity " + transEntity.getTransId(), e);
            }

            logger.info("Consume KafkaScaleConsumer.doHandle " + transEntity.getTransId());
            TransProcessedEntity transProcessedEntity = new TransProcessedEntity();
            transProcessedEntity.setTransId(transEntity.getTransId());
            transProcessedEntity.setCreateTime(System.currentTimeMillis());
            try {
                redisCache.cacheObjectWithExpire(processedCacheKey, transProcessedEntity, 20, TimeUnit.MINUTES);
            } catch (Exception e) {
                rootLogger.error("Exception cacheObjectWithExpire " + transEntity.getTransId(), e);
            }

            String cacheKey = redisCache.getAntiLossCacheKey(transEntity.getTransId());
            redisCache.deleteString(cacheKey);
        } finally {
            lock.unlock();
        }
    }
}
