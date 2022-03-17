package vn.zalopay.kafkascale.entity;

import lombok.Data;

@Data
public class TransProcessedEntity {

    private long transId;
    private long createTime;
}
