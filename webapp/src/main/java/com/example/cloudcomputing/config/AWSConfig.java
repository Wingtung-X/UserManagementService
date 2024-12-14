package com.example.cloudcomputing.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AWSConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${management.metrics.export.cloudwatch.namespace}")
    private String nameSpace;

    // Common credentials provider using DefaultCredentialsProvider
    @Bean
    public DefaultCredentialsProvider credentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    // S3 Client Configuration
    @Bean
    public S3Client s3Client(DefaultCredentialsProvider credentialsProvider) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    // CloudWatch Configuration
    @Bean
    public CloudWatchConfig cloudWatchConfig() {
        return new CloudWatchConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String namespace() {
                return nameSpace;
            }
        };
    }

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient(DefaultCredentialsProvider credentialsProvider) {
        return CloudWatchAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    @Primary
    public MeterRegistry meterRegistry(CloudWatchConfig cloudWatchConfig,
                                       CloudWatchAsyncClient cloudWatchAsyncClient) {
        return new CloudWatchMeterRegistry(
                cloudWatchConfig,
                Clock.SYSTEM,
                cloudWatchAsyncClient
        );
    }

    @Bean
    public SnsClient snsClient(DefaultCredentialsProvider credentialsProvider) {
        return SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }


}
