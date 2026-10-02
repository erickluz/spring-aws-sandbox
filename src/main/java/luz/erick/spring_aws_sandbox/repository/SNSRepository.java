package luz.erick.spring_aws_sandbox.repository;

import org.springframework.stereotype.Repository;

import io.awspring.cloud.sns.core.SnsTemplate;
import luz.erick.spring_aws_sandbox.config.SNSConfig;

@Repository 
public class SNSRepository {
    private SnsTemplate snsTemplate;
    private SNSConfig SNSConfig;

    public SNSRepository (SnsTemplate snsTemplate, SNSConfig snsConfig) { 
        this.snsTemplate = snsTemplate;
        this.SNSConfig = snsConfig;
    }

    public void enviarNotificacao(String message) {
        snsTemplate.sendNotification(SNSConfig.getSNSTopicArn(), message, SNSConfig.getSubjectNotificacao());
    }

    public void enviarPedido(String pedido) {
        snsTemplate.sendNotification(SNSConfig.getSNSTopicArn(), SNSConfig.getSubjectPedido(), SNSConfig.getSubjectPedido());
    }
}
