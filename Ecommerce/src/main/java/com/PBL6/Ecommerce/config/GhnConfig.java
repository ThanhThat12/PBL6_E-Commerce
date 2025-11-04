package com.PBL6.Ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GhnConfig {
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(5000);
        rf.setReadTimeout(15000);
        return new RestTemplate(rf);
    }
}