package com.sfbabdi.tltakehome.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Clock;
import java.util.Random;


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
}


