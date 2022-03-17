package vn.zalopay.kafkascale.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.zalopay.kafkascale.entity.Counter;

@Configuration
public class VisibilityConfiguration {

    @Bean
    public Counter counter() {
        return new Counter();
    }
}
