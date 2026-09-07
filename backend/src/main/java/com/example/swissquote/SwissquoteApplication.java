package com.example.swissquote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@EnableConfigurationProperties(com.example.swissquote.application.analysis.AiAnalysisServiceProperties.class)
public class SwissquoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwissquoteApplication.class, args);
    }

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
