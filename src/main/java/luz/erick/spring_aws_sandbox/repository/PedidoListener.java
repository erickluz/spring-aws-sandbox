package luz.erick.spring_aws_sandbox.repository;

import org.springframework.stereotype.Component;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luz.erick.spring_aws_sandbox.domain.Pedido;
import luz.erick.spring_aws_sandbox.domain.PedidoEvento;
import luz.erick.spring_aws_sandbox.service.PedidoService;
import tools.jackson.databind.ObjectMapper;

@Slf4j 
@Component
@RequiredArgsConstructor 
public class PedidoListener {

    private final ObjectMapper objectMapper;
    private final PedidoService pedidoService;


    @SqsListener("pedido-listener") 
    public void pedidoListener(String mensagem) throws Exception {
        PedidoEvento pedidoCriado = toPedidoEvento(mensagem);
        pedidoService.processarPedido(pedidoCriado);
    }

    @SqsListener("pedido-queue") 
    public void pedidoQueueListener(String mensagem) throws Exception {
        Pedido pedido = toPedido(mensagem);
        log.info("Pedido: " + pedido.toString());
    }

    private PedidoEvento toPedidoEvento(String json) throws Exception {
        return objectMapper.readValue(json, PedidoEvento.class);
    }

        private Pedido toPedido(String json) throws Exception {
        return objectMapper.readValue(json, Pedido.class);
    }


}
