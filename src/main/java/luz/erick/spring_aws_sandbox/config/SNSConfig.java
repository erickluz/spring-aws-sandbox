package luz.erick.spring_aws_sandbox.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class SNSConfig {

    @Value("${sns.topic.pedido}")
    private String SNSTopicArn;
    @Value("${sns.subject.pedido}")
    private String subjectPedido;
    @Value("${sns.subject.notificacao}")
    private String subjectNotificacao;
    public String getSNSTopicArn() {
        return SNSTopicArn;
    }
    public String getSubjectPedido() {
        return subjectPedido;
    }
    public String getSubjectNotificacao() {
        return subjectNotificacao;
    }

}
