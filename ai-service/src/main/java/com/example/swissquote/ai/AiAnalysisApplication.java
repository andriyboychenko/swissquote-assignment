package com.example.swissquote.ai;

import com.example.swissquote.infrastructure.ai.DeterministicAiAnalysisGenerator;
import com.example.swissquote.infrastructure.ai.DocumentPolicyKnowledgeRepository;
import com.example.swissquote.infrastructure.persistence.JdbcAiAnalysisRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import java.time.Clock;

@SpringBootApplication(scanBasePackages = "com.example.swissquote.ai")
@EnableConfigurationProperties(AiServiceProperties.class)
@Import({
        JdbcAiAnalysisRepository.class,
        DeterministicAiAnalysisGenerator.class,
        DocumentPolicyKnowledgeRepository.class
})
public class AiAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAnalysisApplication.class, args);
    }

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    Clock systemUtcClock() {
        return Clock.systemUTC();
    }
}
