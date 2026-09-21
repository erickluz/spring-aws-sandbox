package luz.erick.spring_aws_sandbox.repository;


import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import luz.erick.spring_aws_sandbox.config.SqsConfig;
import software.amazon.awssdk.services.sqs.model.Message;

@SpringBootTest
public class SQSRepositoryTest {

    @Autowired
    private SQSRepository sqsRepository;

    @Autowired 
    private SqsConfig sqsConfig;

    @Test 
    void deveCriarEListarQueues() {

        sqsRepository.createQueue("q1");
        sqsRepository.createQueue("q2");

        List<String> queues = sqsRepository.listQueues()
                                .stream()
                                .map(q -> q.replace("http://", ""))
                                .toList();

        String URLServer = sqsConfig.getURL();
        String URLBase = sqsConfig.getUrlBase();

        Assertions.assertEquals(URLServer + "q1", queues.get(0));
        Assertions.assertEquals(URLServer + "q2", queues.get(1));

        String messageTest = "msgTeste1";
        sqsRepository.sendMessage("q1", messageTest);

        List<Message> messages = sqsRepository.receiveMessages(URLBase + "q1");
        Message message = messages.get(0);
        Assertions.assertEquals(messageTest, message.body());

        sqsRepository.deleteMessage(URLBase + "q1", Lists.newArrayList(message));

        messages = sqsRepository.receiveMessages(URLBase + "q1");

        Assertions.assertTrue(messages.isEmpty());

        sqsRepository.deleteQueue(URLBase + "q1"); 
        sqsRepository.deleteQueue(URLBase + "q2"); 

        Assertions.assertTrue(sqsRepository.listQueues().isEmpty());
    }

}