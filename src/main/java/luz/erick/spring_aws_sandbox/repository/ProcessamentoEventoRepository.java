package luz.erick.spring_aws_sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import luz.erick.spring_aws_sandbox.domain.ProcessamentoEvento;
import luz.erick.spring_aws_sandbox.domain.NomeConsumidorEvento;

public interface ProcessamentoEventoRepository extends JpaRepository<ProcessamentoEvento, Long> {

    boolean existsByEventIdAndConsumerName(Long eventId, NomeConsumidorEvento consumerName);

}
