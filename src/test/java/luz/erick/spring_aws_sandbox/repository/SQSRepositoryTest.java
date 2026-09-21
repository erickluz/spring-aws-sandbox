package luz.erick.spring_aws_sandbox.repository;


import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import luz.erick.spring_aws_sandbox.config.SqsConfig;

@SpringBootTest
public class SQSRepositoryTest {

    @Autowired
    private SQSRepository repositorySQS;

    @Autowired 
    private SqsConfig sqsConfig;

    @Test 
    void deveCriarEListarQueues() {

        repositorySQS.createQueue("q1");
        repositorySQS.createQueue("q2");

        List<String> queues = repositorySQS.listQueues()
                                .stream()
                                .map(q -> q.replace("http://", ""))
                                .toList();

        String URLServer = getURL();
        String URLBase = getUrlBase();

        Assertions.assertEquals(URLServer + "q1", queues.get(0));
        Assertions.assertEquals(URLServer + "q2", queues.get(1));

        String messageTest = "msgTeste1";
        repositorySQS.sendMessage("q1", messageTest);

        List<String> messages = repositorySQS.receiveMessages(URLBase + "q1");
        String message = messages.get(0);
        Assertions.assertEquals(messageTest, message);

        repositorySQS.deleteQueue(URLBase + "q1"); 
        repositorySQS.deleteQueue(URLBase + "q2"); 

        Assertions.assertTrue(repositorySQS.listQueues().isEmpty());
    }

    private String getUrlBase() {
        return sqsConfig.getAWSEndpoint() + "/" + sqsConfig.getAWSAccountId() + "/";
    }

    private String getURL() {
        return "sqs." + sqsConfig.getAWSRegion() + "." + sqsConfig.getAWSEndpoint().replace("http://", "") +  "/" + sqsConfig.getAWSAccountId() + "/";
    }

}