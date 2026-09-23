package luz.erick.spring_aws_sandbox.repository;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
public class SQSSpringCloudListener {

    @SqsListener("q3")
    public void listen(String message) {
       log.info("Mensagem recebida SQS: " + message); 
    }

}
