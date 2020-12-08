package com.sfbabdi.tltakehome.configuration;

import com.sfbabdi.tltakehome.model.PixelCheckResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Clock;
import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


@Slf4j
@Configuration
@AllArgsConstructor
public class ApplicationConfiguration {

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public UrlValidator configureUrlValidator() {
        String[] schemes = {"http", "https"};
        return new UrlValidator(schemes);
    }

    @Bean
    public Random configureRandom() {
        return new Random();
    }

    @Bean
    public WebClient configureWebClient() {
        // Can also configure timeout here if necessary
        return WebClient.create();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ThreadPoolExecutor configureThreadPool() {
        return (ThreadPoolExecutor)Executors.newFixedThreadPool(100);
    }

    @Bean
    public CompletionService<PixelCheckResult> configureCompletionService(ThreadPoolExecutor executor) {
        return new ExecutorCompletionService<>(executor);
    }
}


