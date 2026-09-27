package luz.erick.spring_aws_sandbox.domain;

import java.time.LocalDateTime;

public record PedidoEvento(
    Long eventId, 
    Long pedidoId, 
    Long clientId, 
    String cpfValido, 
    boolean estoqueDisponivel,
    boolean requerAnaliseAntifraude,
    LocalDateTime agendadoPara
) {}
