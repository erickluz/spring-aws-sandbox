package luz.erick.spring_aws_sandbox.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration 
public class SqsConfig {

    @Value("${aws.sqs.endpoint}")
    private String awsSqsEndpoint;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.accountId}")
    private String accountId;

    @Bean
    SqsClient SqsClient() {
        // LOCALSTACK
        System.out.println("REGIAO: " + awsRegion);
        System.out.println("ENDPOINT: " + awsSqsEndpoint);
        return SqsClient.builder()
        .endpointOverride(URI.create(awsSqsEndpoint))
        .region(Region.of(awsRegion))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accountId, "test")
        ))
        .build();
    }

    public String getAWSAccountId() {
        return this.accountId;
    }

    public String getAWSEndpoint() {
        return this.awsSqsEndpoint;
    }

    public String getAWSRegion() {
        return this.awsRegion;
    }

    public String getUrlBase() {
        return getAWSEndpoint() + "/" + getAWSAccountId() + "/";
    }

    public String getURL() {
        return "sqs." + getAWSRegion() + "." + getAWSEndpoint().replace("http://", "") +  "/" + getAWSAccountId() + "/";
    }

}
