package com.PBL6.Ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CohereConfig {

    @Value("${cohere.base-url:https://api.cohere.ai/v2/chat}")
    private String baseUrl;

    @Bean(name = "cohereRestTemplate")
    public RestTemplate cohereRestTemplate() {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(5000);
        rf.setReadTimeout(10000);
        return new RestTemplate(rf);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}

