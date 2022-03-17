package vn.zalopay.kafkascale.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @author anhnq7
 */
@ConfigurationProperties(prefix = "redis")
@Configuration
@Data
public class RedisConfig {

  private boolean cluster;
  private int scanInterval;
  private int slaveConnectionMinimumIdleSize;
  private int slaveConnectionPoolSize;
  private int masterConnectionMinimumIdleSize;
  private int masterConnectionPoolSize;
  private int idleConnectionTimeout;
  private int connectTimeout;
  private int responseTimeout;
  private int retryAttempts;
  private int retryInterval;
  private int reconnectionTimeout;
  private int failedAttempts;
  private String readMode;
  private boolean kryoCodec;
  private String redisNode;
  private int cacheExpireMinute;
  private int lockAcquireTimeMillisecond;
  private String codecType;
  private boolean keepAlive;
  private boolean tcpNoDelay;
  private int pingConnectionInterval;
}
