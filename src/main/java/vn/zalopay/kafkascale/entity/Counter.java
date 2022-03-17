package vn.zalopay.kafkascale.entity;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class Counter {

    private AtomicLong lCounter;

    public Counter() {
        this.lCounter = new AtomicLong();
    }
}
