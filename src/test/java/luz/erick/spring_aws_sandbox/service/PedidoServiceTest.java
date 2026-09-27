package luz.erick.spring_aws_sandbox.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.net.URI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;

class PedidoServiceTest {

    private static final String SQS_ENDPOINT = System.getProperty("aws.sqs.endpoint", "http://localhost.localstack.cloud:4566");
    private static final String AWS_REGION = System.getProperty("aws.region", "sa-east-1");
    private static final String AWS_ACCOUNT_ID = System.getProperty("aws.accountId", "000008845000");
    private static final int VISIBILITY_TIMEOUT_SECONDS = 1;
    private static final int MAX_RECEIVE_COUNT = 2;

    private SqsClient sqsClient;

    private String pedidoQueueName;
    private String pedidoQueueUrl;
    private String pedidoDlqName;
    private String pedidoDlqUrl;

    @BeforeEach
    void criarFilaPrincipalComDlq() {
        sqsClient = SqsClient.builder()
            .endpointOverride(URI.create(SQS_ENDPOINT))
            .region(Region.of(AWS_REGION))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(AWS_ACCOUNT_ID, "test")
            ))
            .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        pedidoQueueName = "pedido-queue-" + suffix;
        pedidoDlqName = "pedido-dlq-" + suffix;

        pedidoDlqUrl = criarFila(pedidoDlqName);
        String pedidoDlqArn = buscarQueueArn(pedidoDlqUrl);

        pedidoQueueUrl = criarFila(pedidoQueueName);

        String redrivePolicy = """
            {
              "deadLetterTargetArn": "%s",
              "maxReceiveCount": "%d"
            }
            """.formatted(pedidoDlqArn, MAX_RECEIVE_COUNT);

        sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
            .queueUrl(pedidoQueueUrl)
            .attributes(Map.of(
                QueueAttributeName.REDRIVE_POLICY, redrivePolicy,
                QueueAttributeName.VISIBILITY_TIMEOUT, String.valueOf(VISIBILITY_TIMEOUT_SECONDS)
            ))
            .build());
    }

    @AfterEach
    void removerFilas() {
        if (pedidoQueueUrl != null) {
            sqsClient.deleteQueue(builder -> builder.queueUrl(pedidoQueueUrl));
        }

        if (pedidoDlqUrl != null) {
            sqsClient.deleteQueue(builder -> builder.queueUrl(pedidoDlqUrl));
        }

        if (sqsClient != null) {
            sqsClient.close();
        }
    }

    @Test
    void deveMoverMensagemParaDlqQuandoNaoForApagadaAposMaxReceiveCount() throws Exception {
        String mensagem = """
            {
              "eventId": 1,
              "pedidoId": 10,
              "clientId": 20,
              "cpfValido": "false",
              "estoqueDisponivel": true,
              "requerAnaliseAntifraude": false,
              "agendadoPara": null
            }
            """;

        sqsClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(pedidoQueueUrl)
            .messageBody(mensagem)
            .build());

        List<Message> primeiraTentativa = receberMensagem(pedidoQueueUrl);
        assertFalse(primeiraTentativa.isEmpty());
        assertEquals(mensagem, primeiraTentativa.get(0).body());

        esperarVisibilityTimeoutExpirar();

        List<Message> segundaTentativa = receberMensagem(pedidoQueueUrl);
        assertFalse(segundaTentativa.isEmpty());
        assertEquals(mensagem, segundaTentativa.get(0).body());

        esperarVisibilityTimeoutExpirar();

        List<Message> mensagensNaFilaPrincipal = receberMensagem(pedidoQueueUrl);
        List<Message> mensagensNaDlq = receberMensagem(pedidoDlqUrl);

        assertTrue(mensagensNaFilaPrincipal.isEmpty());
        assertFalse(mensagensNaDlq.isEmpty());
        assertEquals(mensagem, mensagensNaDlq.get(0).body());
    }

    private String criarFila(String queueName) {
        sqsClient.createQueue(CreateQueueRequest.builder()
            .queueName(queueName)
            .build());

        return sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
            .queueName(queueName)
            .build())
            .queueUrl();
    }

    private String buscarQueueArn(String queueUrl) {
        return sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder()
            .queueUrl(queueUrl)
            .attributeNames(QueueAttributeName.QUEUE_ARN)
            .build())
            .attributes()
            .get(QueueAttributeName.QUEUE_ARN);
    }

    private List<Message> receberMensagem(String queueUrl) {
        return sqsClient.receiveMessage(ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(1)
            .waitTimeSeconds(1)
            .build())
            .messages();
    }

    private void esperarVisibilityTimeoutExpirar() throws InterruptedException {
        Thread.sleep((VISIBILITY_TIMEOUT_SECONDS + 1) * 1000L);
    }
}
