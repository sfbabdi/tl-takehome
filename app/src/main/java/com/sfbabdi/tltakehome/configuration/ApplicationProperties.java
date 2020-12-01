package com.sfbabdi.tltakehome.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "calgw")
public class ApplicationProperties {

    private Validator validator;

    @NotEmpty
    private String environmentName;

    @NotEmpty
    private String creator = System.getProperty("user.name");

    @NotNull
    @Valid
    private ApplicationProperties.SampleProperties netty = new SampleProperties();

    ApplicationProperties(Validator validator) {
        this.validator = validator;
    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SampleProperties {

        @NotEmpty
        private String host = "0.0.0.0";

        @Min(1)
        @Max(65535)
        private int port = 4090;

        @Min(1)
        @Max(65535)
        private int healthCheckPort = 4090;

        @Min(4)
        private int numWorkerThreads = 4;
    }

}


