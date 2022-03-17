package vn.zalopay.kafkascale.entity;

import lombok.Data;

@Data
public class TransEntityRedisWrapper {

    private long updateTime;
    private TransEntity transEntity;
    private int retryTime;
}
