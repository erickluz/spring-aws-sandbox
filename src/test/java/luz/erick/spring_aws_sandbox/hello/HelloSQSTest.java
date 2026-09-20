package luz.erick.spring_aws_sandbox.hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HelloSQSTest {

    @Autowired
    private HelloSQS helloSQS;

    @Test 
    void deveListarQueues() {
        System.out.println("Queues");
        System.out.println(helloSQS.listQueues());
    }

}
