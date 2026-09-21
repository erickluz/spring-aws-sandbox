package luz.erick.spring_aws_sandbox.hello;


import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import luz.erick.spring_aws_sandbox.config.SqsConfig;

@SpringBootTest
public class HelloSQSTest {

    @Autowired
    private HelloSQS helloSQS;

    @Autowired 
    private SqsConfig sqsConfig;

    @Test 
    void deveCriarEListarQueues() {

        helloSQS.createQueue("q1");
        helloSQS.createQueue("q2");

        List<String> queues = helloSQS.listQueues()
                                .stream()
                                .map(q -> q.replace("http://", ""))
                                .toList();

        String URLServer = getURL();
        String URLBase = getUrlBase();

        Assertions.assertEquals(URLServer + "q1", queues.get(0));
        Assertions.assertEquals(URLServer + "q2", queues.get(1));

        String messageTest = "msgTeste1";
        System.out.println("sendmessage");
        helloSQS.sendMessage("q1", messageTest);
        System.out.println("receivemessage");
        List<String> messages = helloSQS.receiveMessages(URLBase + "q1");
        String message = messages.get(0);
        Assertions.assertEquals(messageTest, message);

        helloSQS.deleteQueue(URLBase + "q1"); 
        helloSQS.deleteQueue(URLBase + "q2"); 

        Assertions.assertTrue(helloSQS.listQueues().isEmpty());
    }

    private String getUrlBase() {
        return sqsConfig.getAWSEndpoint() + "/" + sqsConfig.getAWSAccountId() + "/";
    }

    private String getURL() {
        return "sqs." + sqsConfig.getAWSRegion() + "." + sqsConfig.getAWSEndpoint().replace("http://", "") +  "/" + sqsConfig.getAWSAccountId() + "/";
    }

}