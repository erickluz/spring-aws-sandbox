# 📦 Regras de negócio — PedidoService

Este roteiro usa regras fictícias para estudar SQS com JUnit, LocalStack, Spring Cloud AWS e H2.

## 🎯 Limites da POC

- A fila de entrada recebe eventos de domínio relacionados a pedido.
- **PedidoListener** converte a mensagem e chama **PedidoService**.
- **PedidoService** não conhece fila, URL, receipt handle ou política de retry.
- O H2 armazena o estado atual do pedido e os eventos já consumidos.
- O listener confirma a mensagem quando termina sem exceção.
- Esta fase não usa **Outbox Pattern**. Publicação de eventos de saída fica fora do fluxo por enquanto.

## 📚 Vocabulário

| Termo | Significado |
|---|---|
| Evento | Fato recebido pela aplicação, como “pedido criado”. |
| Status | Estado atual e mutável de um pedido no H2. |
| Entrega duplicada | Nova entrega da mesma mensagem pelo SQS; é esperada no modelo *at-least-once*. |
| Consumidor | Responsável lógico que tratou a mensagem. Nesta POC: **PEDIDO_LISTENER**. |
| Estado final | Estado que não aceita eventos posteriores de conclusão: processado, rejeitado ou cancelado. |

Quando um atributo técnico for necessário, este documento informa a sua origem: por exemplo, **PedidoEvento.eventId** ou **Pedido.status**.

## 🧭 Eventos e estados

### Eventos de domínio — enum `TipoEvento`

Os valores desta tabela são declarados em `luz.erick.spring_aws_sandbox.domain.enums.TipoEvento`.

| `TipoEvento` | O que comunica |
|---|---|
| **PEDIDO_CRIADO** | Um novo pedido deve iniciar sua avaliação. |
| **ESTOQUE_RESERVADO** | O estoque foi separado para um pedido que aguardava disponibilidade. |
| **ANALISE_ANTIFRAUDE_APROVADA** | A análise assíncrona aprovou o pedido. |
| **ANALISE_ANTIFRAUDE_REPROVADA** | A análise assíncrona recusou o pedido. |
| **PEDIDO_CANCELADO** | O pedido deve ser encerrado antes da conclusão. |

### Estados de Pedido — enum `StatusPedido`

Os valores desta tabela são declarados em `luz.erick.spring_aws_sandbox.domain.enums.StatusPedido` e são persistidos no atributo `Pedido.status`.

| Pedido.status | Significado |
|---|---|
| **RECEBIDO** | O pedido entrou no fluxo e aguarda a próxima decisão. |
| **AGUARDANDO_ESTOQUE** | O pedido é válido, mas ainda não há estoque. |
| **EM_ANALISE_ANTIFRAUDE** | O pedido aguarda a decisão antifraude. |
| **PROCESSADO** | O pedido concluiu o fluxo com sucesso. Estado final. |
| **REJEITADO** | Cadastro ou análise recusou o pedido. Estado final. |
| **CANCELADO** | O pedido foi cancelado antes da conclusão. Estado final. |

Um evento descreve algo que chegou para ser tratado; um status resume o resultado atual. Exemplo: **ANALISE_ANTIFRAUDE_APROVADA** pode mudar **Pedido.status** para **PROCESSADO**.

## 📨 Mensagem inicial: Pedido criado

| Atributo de PedidoEvento | Uso no cenário |
|---|---|
| **eventId** | Identifica a entrega lógica usada pela idempotência. |
| **pedidoId** | Identifica o pedido de negócio criado ou atualizado. |
| **clientId** | Identifica o cliente associado ao pedido. |
| **cpfValido** | Simula a validação cadastral. |
| **estoqueDisponivel** | Simula disponibilidade de estoque. |
| **requerAnaliseAntifraude** | Define se o pedido segue para análise. |
| **agendadoPara** | Permite simular entrega atrasada. |

## ✅ Regra 1 — Criar e concluir um pedido simples

**Quando:** chega um evento **PEDIDO_CRIADO**, o cadastro é válido, existe estoque e o pedido não requer análise antifraude.

**Então:**

1. Criar **Pedido** com **Pedido.status = RECEBIDO**.
2. Concluir a avaliação e alterar **Pedido.status** para **PROCESSADO**.
3. Registrar a conclusão em **ProcessamentoEvento**, vinculando **PedidoEvento.eventId** ao consumidor **PEDIDO_LISTENER**.
4. O listener termina com sucesso e a entrega é confirmada.

**Cenário JUnit:** publicar um pedido simples e verificar um pedido processado e uma entrega concluída.

## ❌ Regra 2 — Rejeitar cadastro inválido

**Quando:** chega um evento **PEDIDO_CRIADO** cujo **PedidoEvento.cpfValido** indica cadastro inválido.

**Então:**

1. O resultado de negócio é **Pedido.status = REJEITADO**.
2. Para estudar DLQ, o listener pode lançar uma exceção deliberadamente.
3. Como o SQS não conhece "falha definitiva", ele redeliverá a mensagem até a redrive policy movê-la para a DLQ.

**Atenção:** se a gravação do pedido rejeitado e a exceção estiverem na mesma transação, a exceção pode desfazer a gravação. Para preservar o diagnóstico, use transação separada ou trate este caso apenas como exercício de DLQ.

**Cenário JUnit:** configurar limite baixo de recebimentos, publicar pedido inválido e verificar a mensagem na DLQ.

## 🔄 Regra 3 — Aguardar estoque

**Quando:** chega um evento **PEDIDO_CRIADO** cujo **PedidoEvento.estoqueDisponivel** indica falta de estoque.

**Então:**

1. O resultado de negócio é **Pedido.status = AGUARDANDO_ESTOQUE**.
2. Não criar **ProcessamentoEvento**, pois o consumo ainda não foi concluído.
3. O listener falha temporariamente e a mensagem pode reaparecer após o visibility timeout.
4. Quando houver estoque, uma nova tentativa bem-sucedida ou o evento **ESTOQUE_RESERVADO** leva o pedido ao próximo passo.

**Cenário JUnit:** fazer as primeiras tentativas falharem, disponibilizar estoque na tentativa seguinte e verificar uma única conclusão.

## ♻️ Regra 4 — Ignorar entrega duplicada

**Quando:** já existe um **ProcessamentoEvento** com o mesmo identificador de evento e o mesmo consumidor da mensagem recebida.

**Então:**

1. Não alterar **Pedido**.
2. Não criar outro **ProcessamentoEvento**.
3. Encerrar o listener como sucesso técnico, pois o efeito de negócio já ocorreu.

**Cenário JUnit:** reenviar duas vezes o mesmo **PedidoEvento.eventId** e verificar uma única conclusão para **PEDIDO_LISTENER**.

## 🐢 Regra 5 — Encaminhar para análise antifraude

**Quando:** chega um evento **PEDIDO_CRIADO** válido, com estoque, e **PedidoEvento.requerAnaliseAntifraude** é verdadeiro.

**Então:**

1. Criar ou atualizar **Pedido.status** para **EM_ANALISE_ANTIFRAUDE**.
2. O evento **ANALISE_ANTIFRAUDE_APROVADA** muda o pedido para **PROCESSADO**.
3. O evento **ANALISE_ANTIFRAUDE_REPROVADA** muda o pedido para **REJEITADO**.

**Cenário JUnit:** bloquear a análise com CountDownLatch, observar o visibility timeout e concluir com o evento de decisão apropriado.

## 📈 Regra 6 — Processar lote respeitando capacidade

**Quando:** muitos eventos **PEDIDO_CRIADO** válidos chegam em sequência.

**Então:**

1. O listener processa em paralelo somente até a concorrência configurada.
2. Mensagens excedentes permanecem aguardando na fila.
3. Cada pedido mantém a regra de idempotência mesmo com processamento concorrente.

**Cenário JUnit:** publicar cinquenta pedidos, tornar o serviço lento e medir o maior número de execuções simultâneas.

## ⏰ Regra 7 — Entregar pedido no horário agendado

**Quando:** **PedidoEvento.agendadoPara** representa um horário futuro.

**Então:**

1. O produtor calcula o atraso e publica com DelaySeconds.
2. O listener não recebe a mensagem antes do horário esperado.
3. Após a entrega, o pedido segue uma das regras anteriores.

**Cenário JUnit:** publicar uma mensagem atrasada, confirmar que não é consumida imediatamente e verificar o processamento depois.

## 🛑 Regra 8 — Cancelar pedido ainda aberto

**Quando:** chega **PEDIDO_CANCELADO** para um pedido recebido, aguardando estoque ou em análise antifraude.

**Então:**

1. Alterar **Pedido.status** para **CANCELADO**.
2. Registrar a entrega concluída em **ProcessamentoEvento**.
3. Ignorar eventos posteriores que tentem concluir o pedido, pois ele está em estado final.

**Cenário JUnit:** criar pedido aguardando estoque, cancelar e depois publicar uma reserva de estoque. O pedido deve permanecer cancelado.

## 🗃️ Estruturas persistidas no H2

| Entidade / tabela | Responsabilidade |
|---|---|
| Pedido / pedido | Armazena o estado atual do pedido. |
| ProcessamentoEvento / processamento_evento | Registra entregas concluídas e evita repetição de efeitos por consumidor. |

## 🔍 O que cada regra permite estudar

| Regra | Conceitos SQS e Spring Cloud AWS |
|---|---|
| Pedido simples | Listener, conversão de payload e confirmação. |
| Cadastro inválido | Falha, redelivery e DLQ. |
| Aguardar estoque | Visibility timeout e redelivery. |
| Entrega duplicada | Entrega *at-least-once* e idempotência. |
| Análise antifraude | Mensagem em processamento e controle de visibilidade. |
| Lote de pedidos | Concorrência, throughput e backpressure. |
| Pedido agendado | Atraso de mensagem. |
| Cancelamento | Ordem dos eventos, estados finais e idempotência. |
