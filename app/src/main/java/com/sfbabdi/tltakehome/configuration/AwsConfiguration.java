package com.sfbabdi.tltakehome.configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.context.annotation.ConditionalOnAwsCloudEnvironment;
import org.springframework.cloud.aws.context.config.annotation.EnableContextCredentials;
import org.springframework.cloud.aws.context.config.annotation.EnableContextRegion;
import org.springframework.cloud.aws.context.support.env.AwsCloudEnvironmentCheckUtils;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.cloud.aws.core.region.StaticRegionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.Profile;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
public class AwsConfiguration {

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Conditional(OnNotAwsCloudEnvironmentCondition.class)
    public @interface ConditionalOnNotAwsCloudEnvironment {

    }

    /**
     * The AWS configuration to be used within (i.e. internal) AWS.
     */
    @Configuration
    @ConditionalOnAwsCloudEnvironment
    @EnableContextRegion(autoDetect = true)
    @EnableContextCredentials
    public static class InternalAwsConfiguration {

        @Bean
        public AmazonDynamoDB amazonDynamoDb(AWSCredentialsProvider credentialsProvider) {
            return AmazonDynamoDBClientBuilder.standard().withCredentials(credentialsProvider).build();
        }
    }

    /**
     * The AWS configuration to be used outside of (i.e. external) AWS.
     */
    @Configuration
    @ConditionalOnNotAwsCloudEnvironment
    @EnableConfigurationProperties(ApplicationProperties.class)
    @Profile("AWS")
    public static class ExternalAwsConfiguration {

        @Value("${cloud.aws.region.static}")
        private String regionName;

        @Bean
        public RegionProvider regionProvider() {
            return new StaticRegionProvider(regionName);
        }

        @Bean
        public AWSCredentialsProvider awsCredentialsProvider() {
            return new DefaultAWSCredentialsProviderChain();
        }

        @Bean
        public AmazonDynamoDB amazonDynamoDb(AWSCredentialsProvider credentialsProvider) {
            return AmazonDynamoDBClientBuilder.standard().withCredentials(credentialsProvider).build();
        }
    }

    public static class OnNotAwsCloudEnvironmentCondition implements ConfigurationCondition {

        @Override
        public ConfigurationPhase getConfigurationPhase() {
            return ConfigurationPhase.PARSE_CONFIGURATION;
        }

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return !AwsCloudEnvironmentCheckUtils.isRunningOnCloudEnvironment();
        }
    }
}


