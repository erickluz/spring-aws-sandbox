# 📨 Roteiro de estudo — Amazon SQS com Java e Spring

Este roteiro complementa o básico de **enviar** e **consumir** mensagens. A ideia é construir entendimento sobre confiabilidade, escalabilidade e operação de filas em aplicações reais.

## 1. 📬 Produção e consumo básicos

- Criar uma fila e obter sua `queueUrl`.
- Publicar uma mensagem com um produtor.
- Receber mensagens com um consumidor.
- Excluir a mensagem apenas após o processamento terminar com sucesso.

## 2. 🔁 Modelo de entrega e idempotência

- Entender que uma fila Standard usa entrega *at-least-once*.
- Considerar que a mesma mensagem pode ser entregue mais de uma vez.
- Tornar o consumidor idempotente: reprocessar um evento não deve causar efeitos incorretos.
- Usar um identificador de evento, como `eventId`, para detectar duplicidades quando necessário.

## 3. 👻 Visibility timeout

- Entender que uma mensagem recebida fica temporariamente invisível para outros consumidores.
- Configurar um tempo de visibilidade maior que o tempo normal de processamento.
- Renovar a visibilidade quando o processamento for longo.
- Observar o que acontece quando o consumidor falha antes de apagar a mensagem.

## 4. ✅ Confirmação, erros e novas tentativas

- Apagar (`delete`) a mensagem somente após sucesso.
- Diferenciar falhas transitórias de falhas definitivas.
- Permitir que uma mensagem não apagada seja recebida novamente.
- Registrar nos logs o ID da mensagem, o ID do evento e a causa da falha.

## 5. 🗑️ Dead-letter queue (DLQ)

- Criar uma fila principal e uma DLQ.
- Configurar o número máximo de recebimentos antes de mover a mensagem para a DLQ.
- Simular uma mensagem inválida ou um erro persistente.
- Inspecionar, corrigir e reprocessar mensagens que chegaram à DLQ.

## 6. ⏳ Long polling

- Usar `ReceiveMessageWaitTimeSeconds`, preferencialmente até 20 segundos.
- Comparar long polling com consultas curtas.
- Entender como ele reduz respostas vazias, chamadas à AWS e custo.

## 7. 🚦 Filas Standard e FIFO

### Standard

- Alta escala.
- Ordem de entrega não garantida.
- Possibilidade de mensagens duplicadas.

### FIFO

- Ordem preservada dentro de um grupo de mensagens.
- Deduplicação de mensagens.
- Estudar `MessageGroupId` e `MessageDeduplicationId`.
- Conhecer os limites de throughput e quando a garantia de ordem é necessária.

## 8. 🧾 Contrato e atributos da mensagem

- Usar JSON no corpo da mensagem.
- Incluir campos como `eventType`, `eventId`, `occurredAt`, `version` e `data`.
- Usar atributos de mensagem para metadados simples.
- Versionar o payload sem quebrar consumidores existentes.
- Validar e tratar mensagens inválidas.

## 9. ⚙️ Concorrência, lotes e capacidade

- Executar múltiplos consumidores para a mesma fila.
- Receber e apagar mensagens em lotes de até 10.
- Ajustar paralelismo conforme o tempo de processamento e os serviços dependentes.
- Evitar sobrecarregar banco de dados, APIs e outros recursos downstream.

## 10. 🕒 Delay e retenção

- Usar `DelaySeconds` para adiar a disponibilidade de uma mensagem.
- Entender o período de retenção da fila.
- Planejar o que fazer com mensagens que não forem processadas dentro desse período.

## 11. 🔐 Segurança

- Configurar IAM com privilégio mínimo.
- Dar ao produtor somente as permissões necessárias para enviar mensagens.
- Dar ao consumidor somente as permissões necessárias para receber, alterar a visibilidade e apagar mensagens.
- Estudar criptografia no servidor com AWS KMS.
- Nunca armazenar chaves de acesso AWS no código ou no repositório.

## 12. 📊 Observabilidade e operação

- Monitorar mensagens visíveis, mensagens em processamento e idade da mensagem mais antiga.
- Criar alarmes para acúmulo de mensagens e movimentação na DLQ.
- Usar logs estruturados e correlação por `eventId`.
- Definir como investigar e reprocessar falhas em produção.

## 🧪 Exercício integrador

Criar um fluxo de **criação de pedido**:

1. Uma API recebe um pedido e publica o evento `OrderCreated` na fila.
2. Um consumidor processa o evento.
3. Uma falha proposital impede o processamento de uma mensagem.
4. Após as tentativas configuradas, a mensagem chega à DLQ.
5. A aplicação ou um processo administrativo corrige e reprocessa a mensagem.

> 💡 Para código novo, use o AWS SDK for Java 2.x (`software.amazon.awssdk:sqs`). O SDK Java 1.x, atualmente presente no projeto, chegou ao fim de suporte em 31/12/2025.
