package vn.zalopay.kafkascale.entity;

import lombok.Data;

@Data
public class TransEntity {

    private long transId;
    private int appId;
    private String appTransId;
}
