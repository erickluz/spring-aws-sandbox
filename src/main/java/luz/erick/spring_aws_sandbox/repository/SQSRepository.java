package luz.erick.spring_aws_sandbox.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;
import software.amazon.awssdk.services.sqs.paginators.ListQueuesIterable;


@Service  
public class SQSRepository {

    private SqsClient sqsClient;

    public SQSRepository(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public List<String> listQueues() {
        try {
            ListQueuesIterable listQueues = this.sqsClient.listQueuesPaginator();
            return listQueues.stream()
                    .flatMap(r -> r.queueUrls().stream())
                    .map(content -> content.toLowerCase())
                    .collect(Collectors.toList());
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return null;
    }

    public String createQueue(String queueName) {
        try {
            CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                    .queueName(queueName)
                    .build();

            sqsClient.createQueue(createQueueRequest);

            GetQueueUrlResponse getQueueUrlResponse = this.sqsClient
                    .getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
            return getQueueUrlResponse.queueUrl();

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return "";
    }

    public String deleteQueue(String queueUrl) {
        try {
            DeleteQueueRequest createQueueRequest = DeleteQueueRequest.builder()
                    .queueUrl(queueUrl)
                    .build();

            sqsClient.deleteQueue(createQueueRequest);
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return "";
    }

    public void sendMessage(String queueName, String message) {
        try {

            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build();

            String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .build();

            sqsClient.sendMessage(sendMsgRequest);

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }

    public List<String> receiveMessagesString(String queueURL) {
        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueURL)
                    .maxNumberOfMessages(5)
                    .build();
            List<Message> response = this.sqsClient.receiveMessage(receiveMessageRequest).messages();
            return response.stream().map(m -> m.body()).toList();

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return null;
    }

    public List<Message> receiveMessages(String queueURL) {
        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueURL)
                    .maxNumberOfMessages(5)
                    .build();
            return this.sqsClient.receiveMessage(receiveMessageRequest).messages();

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return null;
    }

    public void deleteMessage(String queueURL, List<Message> messages) {
        try {
            for (Message message : messages) {
                DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                        .queueUrl(queueURL)
                        .receiptHandle(message.receiptHandle())
                        .build();
                sqsClient.deleteMessage(deleteMessageRequest);
            }
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }

}
