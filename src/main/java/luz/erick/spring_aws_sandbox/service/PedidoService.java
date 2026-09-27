package luz.erick.spring_aws_sandbox.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import luz.erick.spring_aws_sandbox.domain.NomeConsumidorEvento;
import luz.erick.spring_aws_sandbox.domain.Pedido;
import luz.erick.spring_aws_sandbox.domain.PedidoEvento;
import luz.erick.spring_aws_sandbox.domain.ProcessamentoEvento;
import luz.erick.spring_aws_sandbox.domain.enums.StatusPedido;
import luz.erick.spring_aws_sandbox.domain.enums.TipoAgregado;
import luz.erick.spring_aws_sandbox.domain.enums.TipoEvento;
import luz.erick.spring_aws_sandbox.exception.PedidoException;
import luz.erick.spring_aws_sandbox.repository.PedidoRepository;
import luz.erick.spring_aws_sandbox.repository.ProcessamentoEventoRepository;

@Service 
public class PedidoService {

    private final CpfValidator cpfValidator;
    private final PedidoRepository pedidoRepository;
    private final ProcessamentoEventoRepository processamentoEventoRepository;

    public PedidoService (
        CpfValidator cpfValidator,
        PedidoRepository pedidoRepository,
        ProcessamentoEventoRepository processamentoEventoRepository
    ) {
        this.cpfValidator = cpfValidator;
        this.pedidoRepository = pedidoRepository;
        this.processamentoEventoRepository = processamentoEventoRepository;
    }

    @Transactional
	public void processarPedido(PedidoEvento pedidoEvento) throws PedidoException {
        Pedido pedido = new Pedido(pedidoEvento);
        pedidoRepository.save(pedido);
        validarPedido(pedido, pedidoEvento);
        atualizarStatusPedido(pedido, StatusPedido.PROCESSADO);
        registrarProcessamento(pedidoEvento, TipoEvento.PEDIDO_CRIADO);
	}

    public ProcessamentoEvento registrarProcessamento(PedidoEvento pedidoCriadoEvento, TipoEvento tipoEvento) {
        ProcessamentoEvento processamentoEvento = new ProcessamentoEvento(
            pedidoCriadoEvento.eventId(),
            tipoEvento, 
            TipoAgregado.PEDIDO,
            pedidoCriadoEvento.pedidoId(),
            NomeConsumidorEvento.PEDIDO_LISTENER);
        
        return processamentoEventoRepository.save(processamentoEvento);
    }

    private void validarPedido(Pedido pedido, PedidoEvento pedidoEvento) throws PedidoException {
        if (isPedidoEventoJaExiste(pedidoEvento)) {
            throw new PedidoException("Mensagem Invalida");
        }
        if (!cpfValidator.isValid(pedido.getCpfValido())) {
            atualizarStatusPedido(pedido, StatusPedido.REJEITADO);
            throw new PedidoException("Cpf Invalido");
        }
        if (!pedidoEvento.estoqueDisponivel()) {
            throw new PedidoException("Estoque nao esta disponivel");
        }
    }

    private boolean isPedidoEventoJaExiste(PedidoEvento pedidoEvento) {
        return processamentoEventoRepository.existsByEventIdAndConsumerName(pedidoEvento.eventId(), NomeConsumidorEvento.PEDIDO_LISTENER);
    }

    private Pedido atualizarStatusPedido(Pedido pedido, StatusPedido status) {
        pedido.setStatus(status);
        return pedidoRepository.save(pedido);
    }
}

