package vn.zalopay.kafkascale.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import vn.zalopay.kafkascale.entity.KafkaMessage;
import vn.zalopay.kafkascale.entity.TransEntity;
import vn.zalopay.kafkascale.util.GsonUtil;

@Service
public class KafkaProducer {

    Logger logger = LoggerFactory.getLogger("producer");

    private final KafkaTemplate kafkaTemplate;

    public KafkaProducer(@Qualifier("kafkaScale") KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${kafka.topic.name}")
    public String topic;

    public void send(TransEntity transEntity) {
        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setData(GsonUtil.toJsonString(transEntity));
        kafkaMessage.setCorrelationId("");

        try {
            ListenableFuture future = kafkaTemplate.send(topic, GsonUtil.toJsonString(kafkaMessage));
            future.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
                @Override
                public void onSuccess(SendResult<Integer, String> integerStringSendResult) {
                    logger.info("Success KafkaProducer.send " + transEntity.getTransId());
                }

                @Override
                public void onFailure(Throwable throwable) {
                    logger.error("Failure KafkaProducer.send " + transEntity.getTransId());
                }
            });
        } catch (Exception e) {
            logger.error("Exception KafkaProducer.send " + transEntity.getTransId(), e);
        }
    }
}
