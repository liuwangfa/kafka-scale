package vn.zalopay.kafkascale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import vn.zalopay.kafkascale.entity.Counter;

@SpringBootApplication
public class KafkaScaleApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaScaleApplication.class, args);
	}
}
