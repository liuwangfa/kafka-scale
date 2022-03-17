package vn.zalopay.kafkascale.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.zalopay.kafkascale.entity.Counter;
import vn.zalopay.kafkascale.entity.TransEntity;
import vn.zalopay.kafkascale.producer.KafkaProducer;
import vn.zalopay.kafkascale.util.GsonUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/publish/")
public class PublishController {

    private final KafkaProducer kafkaProducer;
    private final Counter counter;

    @Autowired
    public PublishController(KafkaProducer kafkaProducer, Counter counter) {
        this.kafkaProducer = kafkaProducer;
        this.counter = counter;
    }

    @GetMapping(value = "/**",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> handleGetRequest(HttpServletRequest request,
                                              @RequestParam Map<String, String> requestParams) {

        TransEntity transEntity = new TransEntity();
        transEntity.setTransId(counter.getLCounter().incrementAndGet());
        transEntity.setAppId(1);
        transEntity.setAppTransId(System.currentTimeMillis() + "");
        kafkaProducer.send(transEntity);

        String response = GsonUtil.toJsonString(transEntity);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
