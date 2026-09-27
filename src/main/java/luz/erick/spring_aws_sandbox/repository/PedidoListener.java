package luz.erick.spring_aws_sandbox.repository;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import luz.erick.spring_aws_sandbox.domain.PedidoEvento;
import luz.erick.spring_aws_sandbox.service.PedidoService;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor 
public class PedidoListener {

    private final ObjectMapper objectMapper;
    private final PedidoService pedidoService;


    @SqsListener("pedido-listener") 
    public void pedidoListener(String mensagem) throws Exception {
        PedidoEvento pedidoCriado = toPedido(mensagem);
        pedidoService.processarPedido(pedidoCriado);
    }

    private PedidoEvento toPedido(String json) throws Exception {
        return objectMapper.readValue(json, PedidoEvento.class);
    }
}
