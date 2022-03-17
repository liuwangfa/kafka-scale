package vn.zalopay.kafkascale.cache.redis;

import org.redisson.config.Config;
import vn.zalopay.kafkascale.config.RedisConfig;

/**
 * @author anhnq7
 */

public abstract class BaseRedis<CONF extends RedisConfig> {

  protected CONF redisConfig;
  protected final String ADAPTER = "adapter";
  protected final String TPE = "tpe";

  protected Config initRedissonConf(Config config) {
    String singleServer = redisConfig.getRedisNode();
    config.useSingleServer().setAddress(singleServer)
            .setConnectionMinimumIdleSize(redisConfig.getMasterConnectionMinimumIdleSize())
            .setConnectionPoolSize(redisConfig.getMasterConnectionPoolSize())
            .setIdleConnectionTimeout(redisConfig.getIdleConnectionTimeout())
            .setConnectTimeout(redisConfig.getConnectTimeout())
            .setTimeout(redisConfig.getResponseTimeout())
            .setRetryAttempts(redisConfig.getRetryAttempts())
            .setTcpNoDelay(redisConfig.isTcpNoDelay())
            .setKeepAlive(redisConfig.isKeepAlive())
            .setPingConnectionInterval(redisConfig.getPingConnectionInterval())
            .setRetryInterval(redisConfig.getRetryInterval());
    return config;
  }
}
