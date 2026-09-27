package luz.erick.spring_aws_sandbox.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import luz.erick.spring_aws_sandbox.domain.enums.StatusPedido;

@Entity
@Table(name = "pedido", uniqueConstraints = @UniqueConstraint(name = "uk_pedido_pedido_id", columnNames = "pedido_id"))
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false, updatable = false)
    private Long pedidoId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "cpf_valido", nullable = false)
    private String cpfValido;

    @Column(name = "estoque_disponivel", nullable = false)
    private boolean estoqueDisponivel;

    @Column(name = "requer_analise_antifraude", nullable = false)
    private boolean requerAnaliseAntifraude;

    @Column(name = "agendado_para")
    private LocalDateTime agendadoPara;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Pedido() {
    }

    public Pedido(PedidoEvento evento) {
        this.pedidoId = evento.pedidoId();
        this.clienteId = evento.clientId();
        this.cpfValido = evento.cpfValido();
        this.estoqueDisponivel = evento.estoqueDisponivel();
        this.requerAnaliseAntifraude = evento.requerAnaliseAntifraude();
        this.agendadoPara = evento.agendadoPara();
        this.status = StatusPedido.RECEBIDO;
    }

    @PrePersist
    void created() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updated() {
        updatedAt = LocalDateTime.now();
    }

    public void aguardarEstoque() {
        status = StatusPedido.AGUARDANDO_ESTOQUE;
    }

    public void iniciarAnaliseAntifraude() {
        status = StatusPedido.EM_ANALISE_ANTIFRAUDE;
    }

    public void processar() {
        status = StatusPedido.PROCESSADO;
    }

    public void rejeitar() {
        status = StatusPedido.REJEITADO;
    }

    public void cancelar() {
        status = StatusPedido.CANCELADO;
    }

    public void setStatus(StatusPedido status) { this.status = status; }
    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public Long getClienteId() { return clienteId; }
    public String getCpfValido() { return cpfValido; }
    public boolean isEstoqueDisponivel() { return estoqueDisponivel; }
    public boolean isRequerAnaliseAntifraude() { return requerAnaliseAntifraude; }
    public LocalDateTime getAgendadoPara() { return agendadoPara; }
    public StatusPedido getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
