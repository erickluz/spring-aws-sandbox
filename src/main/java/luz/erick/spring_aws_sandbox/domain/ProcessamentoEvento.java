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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import luz.erick.spring_aws_sandbox.domain.enums.TipoAgregado;
import luz.erick.spring_aws_sandbox.domain.enums.TipoEvento;

@Entity
@Table(
    name = "processamento_evento",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_processamento_evento_evento_consumidor",
        columnNames = {"event_id", "consumer_name"}
    )
)
public class ProcessamentoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private Long eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private TipoEvento eventType;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private TipoAgregado aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "consumer_name", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private NomeConsumidorEvento consumerName;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;

    protected ProcessamentoEvento() {
    }

    public ProcessamentoEvento(
        Long eventId,
        TipoEvento eventType,
        TipoAgregado aggregateType,
        Long aggregateId,
        NomeConsumidorEvento consumerName
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.consumerName = consumerName;
    }

    @PrePersist
    void registrarProcessamento() {
        processedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public TipoEvento getEventType() { return eventType; }
    public TipoAgregado getAggregateType() { return aggregateType; }
    public Long getAggregateId() { return aggregateId; }
    public NomeConsumidorEvento getConsumerName() { return consumerName; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
