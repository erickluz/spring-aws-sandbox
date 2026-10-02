package luz.erick.spring_aws_sandbox.repository;

import org.springframework.stereotype.Component;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component 
@RequiredArgsConstructor 
public class NotificacaoListener {
    
    @SqsListener("notificacao-queue") 
    private void notificacaoListener(String mensagem) {
        log.info("Notificacao: " + mensagem);
    }
}
