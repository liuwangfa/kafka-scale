package vn.zalopay.kafkascale.threadpool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.zalopay.kafkascale.monitor.ThreadPoolMonitor;

@Service
public class KafkaScaleThreadPool extends ThreadPoolMonitor {

    private static String POOL_NAME = "KafkaScaleThreadPool";

    public KafkaScaleThreadPool(@Value("${thread-pool.poolSize}") int poolSize,
                                @Value("${thread-pool.queueSize}") int queueSize) {
        super(POOL_NAME, poolSize, queueSize);
    }

    public void run(Runnable runnable) {
        executor.execute(runnable);
    }
}