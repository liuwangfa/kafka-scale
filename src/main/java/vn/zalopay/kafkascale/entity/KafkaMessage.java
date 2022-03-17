package vn.zalopay.kafkascale.entity;

import lombok.Data;

@Data
public class KafkaMessage {

  private long sendTime = System.currentTimeMillis();
  private String data = "";
  private String correlationId;
}
