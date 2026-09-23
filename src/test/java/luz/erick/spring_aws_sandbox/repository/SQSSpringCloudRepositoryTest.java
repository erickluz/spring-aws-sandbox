package luz.erick.spring_aws_sandbox.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@SpringBootTest
public class SQSSpringCloudRepositoryTest {

    @Autowired
    private SQSSpringCloudRepository sqsSpringCloudRepository;

    private final Logger listenerLogger = (Logger) LoggerFactory.getLogger(SQSSpringCloudListener.class);
    private ListAppender<ILoggingEvent> logs;

    @AfterEach
    void stopLogCapture() {
        if (logs != null) {
            listenerLogger.detachAppender(logs);
            logs.stop();
        }
    }

    @Test
    void devePublicarEConsumirMensagem() throws InterruptedException {
        String message = "msg-" + UUID.randomUUID();
        logs = new ListAppender<>();
        logs.start();
        listenerLogger.addAppender(logs);

        sqsSpringCloudRepository.sendMessage("q3", message);

        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (Instant.now().isBefore(deadline) && !messageFoiConsumida(message)) {
            Thread.sleep(100);
        }

        assertTrue(messageFoiConsumida(message), "A mensagem deveria ser processada pelo listener SQS");
    }

    private boolean messageFoiConsumida(String message) {
        String expectedLog = "Mensagem recebida SQS: " + message;
        return logs.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(expectedLog::equals);
    }

}
