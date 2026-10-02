package luz.erick.spring_aws_sandbox.service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import luz.erick.spring_aws_sandbox.domain.Pedido;
import luz.erick.spring_aws_sandbox.domain.enums.StatusPedido;

@SpringBootTest 
public class LojaServiceTest {

    @Autowired 
    private LojaService lojaService;
    
    @Test 
    void devePublicarPedido() {

        Pedido pedido = new Pedido(
            null, 
            1L, 
            1L, 
            "90860096114", 
            true, 
            false, 
            LocalDateTime.now(), 
            StatusPedido.RECEBIDO);

        lojaService.enviarPedido(pedido);
    }


}
