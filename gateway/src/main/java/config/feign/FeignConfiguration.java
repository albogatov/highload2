package config.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class FeignConfiguration {
    @Bean
    public ErrorDecoder errorDecoder() {
        return null;
    }
}
