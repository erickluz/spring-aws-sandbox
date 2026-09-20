package luz.erick.spring_aws_sandbox.hello;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SqsException;
import software.amazon.awssdk.services.sqs.paginators.ListQueuesIterable;


@Service 
public class HelloSQS {

    private SqsClient sqsClient;

    public HelloSQS(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public List<String> listQueues() {
        try {
            ListQueuesIterable listQueues = this.sqsClient.listQueuesPaginator();
            return listQueues.stream()
                    .flatMap(r -> r.queueUrls().stream())
                    .map(content -> " Queue URL: " + content.toLowerCase())
                    .collect(Collectors.toList());
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return null;
    }

}
