package luz.erick.spring_aws_sandbox.repository;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;
import luz.erick.spring_aws_sandbox.config.SqsConfig;
import software.amazon.awssdk.services.sqs.model.Message;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest 
@Slf4j 
public class SQSIdempotenciaTest {

    @Autowired
    private SQSRepository sqsRepository;

    @Autowired 
    private SqsConfig sqsConfig;

    @Autowired 
    private ObjectMapper objectMapper;

    List<String> messagesId;

    @Test 
    void deveConsumirMensagemComIdempotencia() {
        String URLBase = sqsConfig.getUrlBase();
        messagesId = new ArrayList<String>();
        sqsRepository.createQueue("q1");

        String msg1 = """
            {
                "eventId": 1, 
                "cliente":
                    {
                        "nome": "Erick",
                        "idade": 20
                    }
            }""";
        sqsRepository.sendMessage("q1", msg1);

        List<Message> receivedMessages = sqsRepository.receiveMessages(URLBase + "q1");
        List<Cliente> clientes = processarMensagens(receivedMessages, URLBase);

        Assertions.assertThrows(IllegalArgumentException.class, () -> processarMensagens(receivedMessages, URLBase));
        
        log.info("Clientes: {}", clientes);
    }

    private List<Cliente> processarMensagens(List<Message> messages, String URLBase) {
        List<Cliente> mensagens = new ArrayList<Cliente>();
        log.info("receivedMessages: {}", messages);
        messages.forEach(m -> {
            Evento evento = parseEvento(m);
            if (messagesId.contains(evento.eventId())) {
                sqsRepository.deleteMessage(URLBase + "q1", Lists.list(m));
                throw new IllegalArgumentException();
            }
            messagesId.add(evento.eventId());
            mensagens.add(evento.cliente());
            sqsRepository.deleteMessage(URLBase + "q1", messages);
        });
        log.info("Messagesid: {}", messagesId);
        return mensagens;
    }

    private Evento parseEvento(Message m) {
        Evento evento;
        try {
            evento = toEvento(m.body());
        } catch (Exception e) {
            log.error("Erro ao processar evento", e);
            throw new IllegalArgumentException();
        }
        return evento;
    }

    public Evento toEvento(String json) throws Exception {
        return objectMapper.readValue(json, Evento.class);
    }
}
record Evento (String eventId, Cliente cliente) {};
record Cliente (String nome, Integer idade) {}