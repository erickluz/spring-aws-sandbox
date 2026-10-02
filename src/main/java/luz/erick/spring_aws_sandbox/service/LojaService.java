package luz.erick.spring_aws_sandbox.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import luz.erick.spring_aws_sandbox.domain.Pedido;
import luz.erick.spring_aws_sandbox.repository.SNSRepository;
import tools.jackson.databind.ObjectMapper;

@Service 
public class LojaService {

    private SNSRepository snsRepository;
    private final ObjectMapper objectMapper;

    public LojaService(SNSRepository snsRepository, ObjectMapper objectMapper) {
        this.snsRepository = snsRepository;
        this.objectMapper = objectMapper;
    }

    public void enviarPedido(Pedido pedido) {
        String json = objectMapper.writeValueAsString(pedido);
        snsRepository.enviarPedido(json);
    }

}
