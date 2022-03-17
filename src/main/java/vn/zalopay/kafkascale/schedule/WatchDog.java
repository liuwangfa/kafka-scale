package vn.zalopay.kafkascale.schedule;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import vn.zalopay.kafkascale.cache.redis.RedisCache;
import vn.zalopay.kafkascale.entity.TransEntity;
import vn.zalopay.kafkascale.entity.TransEntityRedisWrapper;
import vn.zalopay.kafkascale.producer.KafkaProducer;
import vn.zalopay.kafkascale.util.GsonUtil;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class WatchDog implements InitializingBean {

    Logger rootLogger = LoggerFactory.getLogger(WatchDog.class);
    Logger logger = LoggerFactory.getLogger("watchdog");

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    private RedisCache redisCache;
    private final KafkaProducer kafkaProducer;

    private int fiveMinuteInMs = 5 * 60 * 1000;

    public WatchDog(RedisCache redisCache, KafkaProducer kafkaProducer) {
        this.redisCache = redisCache;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void afterPropertiesSet() {
        scheduledExecutor.scheduleWithFixedDelay(() -> {
            try {
                List<TransEntityRedisWrapper> transEntityRedisWrapperList = redisCache.listObjects(TransEntityRedisWrapper.class);
                if (transEntityRedisWrapperList != null && !transEntityRedisWrapperList.isEmpty()) {
                    for (TransEntityRedisWrapper transEntityRedisWrapper : transEntityRedisWrapperList) {
                        if (System.currentTimeMillis() - transEntityRedisWrapper.getUpdateTime() > fiveMinuteInMs) {
                            TransEntity transEntity = transEntityRedisWrapper.getTransEntity();
                            logger.info("WatchDog kafkaProducer.send " + transEntity.getTransId());
                            kafkaProducer.send(transEntity);
                        }
                    }
                }
            } catch (Exception e) {
                rootLogger.error("Exception listObjects", e);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
