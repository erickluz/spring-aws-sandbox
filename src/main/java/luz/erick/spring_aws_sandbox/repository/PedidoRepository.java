package luz.erick.spring_aws_sandbox.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import luz.erick.spring_aws_sandbox.domain.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByPedidoId(Long pedidoId);
}
