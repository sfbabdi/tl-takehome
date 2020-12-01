package com.sfbabdi.tltakehome.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationConfiguration {

    private final ApplicationProperties applicationProperties;
    private final List<Tag> tags;

    public ApplicationConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        tags = Arrays.asList(
                Tag.of("environment_name", this.applicationProperties.getEnvironmentName()),
                Tag.of("creator", this.applicationProperties.getCreator())
        );

        log.info("Environment: {}", this.applicationProperties.getEnvironmentName());
    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configureMeterRegistry() {
        return registry -> registry.config().commonTags(tags);
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}


