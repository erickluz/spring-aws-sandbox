package luz.erick.spring_aws_sandbox.repository;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;
import luz.erick.spring_aws_sandbox.config.SqsConfig;
import software.amazon.awssdk.services.sqs.model.Message;

@SpringBootTest 
@Slf4j 
public class SQSIdempotenciaTest {

    @Autowired
    private SQSRepository sqsRepository;

    @Autowired 
    private SqsConfig sqsConfig;

    List<String> messagesId;

    @Test 
    void deveConsumirMensagemComIdempotencia() {

        messagesId = new ArrayList<String>();

        sqsRepository.createQueue("q1");

        String msg1 = "msg1";
        String msg2 = "msg2";
        sqsRepository.sendMessage("q1", msg1);
        sqsRepository.sendMessage("q1", msg2);

        String URLBase = sqsConfig.getUrlBase();
        List<Message> receivedMessages = sqsRepository.receiveMessages(URLBase + "q1");
        List<Message> mensagens = processarMensagens(receivedMessages, URLBase);

        // Deve verificar idempotencia
        Assertions.assertThrows(IllegalArgumentException.class, () -> processarMensagens(receivedMessages, URLBase));
        
        log.info("Mensagens: {}", mensagens);
    }

    private List<Message> processarMensagens(List<Message> messages, String URLBase) {
        List<Message> mensagens = new ArrayList<Message>();
        log.info("recevedMessages: {}", messages);
        messages.forEach(m -> {
            if (messagesId.contains(m.messageId())) {
                throw new IllegalArgumentException();
            }
            messagesId.add(m.messageId());
            mensagens.add(m);
            sqsRepository.deleteMessage(URLBase + "q1", messages);
        });
        log.info("Messagesid: {}", messagesId);
        return mensagens;
    }
}
