package vn.zalopay.kafkascale.monitor;

import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolMonitor implements InitializingBean {

  private final String name;
  private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
  public final ThreadPoolExecutor executor;

  private final AtomicInteger corePoolSize = new AtomicInteger(0);
  private final AtomicInteger largestPoolSize = new AtomicInteger(0);
  private final AtomicInteger maximumPoolSize = new AtomicInteger(0);
  private final AtomicInteger activeCount = new AtomicInteger(0);
  private final AtomicInteger poolSize = new AtomicInteger(0);
  private final AtomicInteger queueSize = new AtomicInteger(0);

  public ThreadPoolMonitor(String name, int poolSize, int queueSize) {
    this.name = name;
    this.executor = new ThreadPoolExecutor(
            poolSize, poolSize, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(queueSize), new ThreadFactory() {
      private final AtomicInteger counter = new AtomicInteger();

      @Override
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(name + "-" + counter.getAndIncrement());
        return thread;
      }
    });

    this.executor.setRejectedExecutionHandler(
            (runnable, executor) -> {
              try {
                executor.getQueue().put(runnable);
              } catch (Exception e) {
                Thread.currentThread().interrupt();
              }
            }
    );
  }

  @Override
  public void afterPropertiesSet() {
    scheduledExecutor.scheduleWithFixedDelay(() -> {
      try {
        corePoolSize.set(executor.getCorePoolSize());
        largestPoolSize.set(executor.getLargestPoolSize());
        maximumPoolSize.set(executor.getMaximumPoolSize());
        activeCount.set(executor.getActiveCount());
        poolSize.set(executor.getPoolSize());
        queueSize.set(executor.getQueue().size());

//        System.out.println("corePoolSize=" + corePoolSize);
//        System.out.println("largestPoolSize=" + largestPoolSize);
//        System.out.println("maximumPoolSize=" + maximumPoolSize);
//        System.out.println("activeCount=" + activeCount);
//        System.out.println("poolSize=" + poolSize);
//        System.out.println("queueSize=" + queueSize);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }, 0, 5, TimeUnit.SECONDS);
  }
}
