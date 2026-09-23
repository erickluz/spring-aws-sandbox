package luz.erick.spring_aws_sandbox.repository;

import org.springframework.stereotype.Repository;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@Repository  
public class SQSSpringCloudRepository {

    private SqsTemplate sqsTemplate;

    public SQSSpringCloudRepository(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }

    public void sendMessage(String queue, String message) {
        sqsTemplate.send(to -> to.queue(queue).payload(message));
    }

}
