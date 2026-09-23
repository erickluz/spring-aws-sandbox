package luz.erick.spring_aws_sandbox.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.awspring.cloud.sqs.config.SqsBootstrapConfiguration;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.awspring.cloud.sqs.operations.TemplateAcknowledgementMode;
import luz.erick.spring_aws_sandbox.repository.SQSSpringCloudListener;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@Import(SqsBootstrapConfiguration.class)
public class SQSSpringCloudConfig {

    private SqsConfig sqsConfig;

    public SQSSpringCloudConfig(SqsConfig sqsConfig) {
        this.sqsConfig = sqsConfig;
    }

    @Bean 
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory() {
        return SqsMessageListenerContainerFactory
            .builder()
            .sqsAsyncClient(sqsAsyncClient())
            .build();
    }

    @Bean 
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
        .endpointOverride(URI.create(sqsConfig.getAWSEndpoint()))
        .region(Region.of(sqsConfig.getAWSRegion()))
        .credentialsProvider(
                        StaticCredentialsProvider.create(
                AwsBasicCredentials.create(sqsConfig.getAWSAccountId(), "test")
        ))
        .build();
    }

    @Bean 
    public SQSSpringCloudListener sqsSpringCloudListener() {
        return new SQSSpringCloudListener();
    }

    @Bean 
    public SqsTemplate sqsTemplate() {
        return SqsTemplate
        .builder()
        .sqsAsyncClient(sqsAsyncClient())
        .configure(o -> o.acknowledgementMode(TemplateAcknowledgementMode.MANUAL))
        .build();
    }

}
