# 🛡️ Credit Analysis System - MVP

Este projeto é um ecossistema de microsserviços voltado para a **Análise de Crédito em tempo real**, focado em alta disponibilidade, resiliência e observabilidade. O sistema utiliza uma arquitetura orientada a eventos e orquestração de processos para gerenciar o ciclo de vida de propostas de crédito.

---

## 🏗️ Arquitetura e Tecnologias

O sistema utiliza padrões arquiteturais modernos para garantir desacoplamento e escalabilidade:

*   **Java 21 & Spring Boot 3.4+**: Core da aplicação.
*   **Apache Kafka**: Mensageria assíncrona entre serviços.
*   **Camunda BPM**: Orquestrador de processos (BPMN).
*   **Redis**: Cache de Scoring e Rate Limit (Segurança).
*   **PostgreSQL**: Dados relacionais e auditoria de fraude.
*   **MongoDB**: Histórico de notificações e alta disponibilidade de escrita.
*   **Prometheus & Grafana**: Monitoramento e métricas em tempo real.
*   **Server-Sent Events (SSE)**: Feedback instantâneo para o front-end.

### 🧩 Microsserviços e Design Patterns
*   **ms-credit**: Porta de entrada. Aplica **Rate Limit** com Redis.
*   **ms-fraud**: Motor de segurança. Usa **Chain of Responsibility** para validações de Blacklist e Renda.
*   **ms-scoring**: Motor de crédito. Usa **Strategy Pattern** para cálculos de score e cache em Redis.
*   **ms-notification**: Entrega de resultados e histórico com **Handshake SSE** via MongoDB.
*   **camunda-starter**: Ponte de eventos entre Kafka e o motor de processos.

![Arquitetura do Sistema](assets/arquitetura.png)



---

## 🚀 Como Executar o Projeto

### Pré-requisitos
*   Docker e Docker Compose.
*   Java 21 (LTS).
*   IDE (IntelliJ IDEA recomendada).
*   **Camunda Modeler** (Download: [Camunda](https://://camunda.com/)).

### 1. Subir a Infraestrutura (Docker)
Na raiz do projeto, execute o comando para subir os bancos e ferramentas de mensageria:
```bash
docker-compose up -d
````
Certifique-se de que os containers do Postgres, Mongo, Redis, Kafka e Camunda estejam "Running".
## 2. Deploy do Fluxo no Camunda (Importante!)

O Camunda Modeler é a ferramenta necessária para desenhar e publicar o fluxo BPMN no motor de processos. Como o sistema é orquestrado, os microsserviços dependem que o fluxo esteja deployado para saberem quando atuar (External Tasks).

**Como fazer o deploy:**
1. Abra o Camunda Modeler.
2. Abra o arquivo `.bpmn` localizado em `/camunda/src/main/resources`.
3. Clique no ícone de Foguete (**Deploy Current Diagram**).
4. Configure o REST Endpoint para: `http://localhost:8080/engine-rest`.
5. Clique em **Deploy**. *Sem este passo, o sistema receberá as propostas, mas o processo não será iniciado.*

![Fluxo BPMN no Camunda](assets/workflowcamunda.png)


## 3. Executar os Microsserviços

Na sua IDE, execute a classe principal de cada serviço na ordem sugerida:
- **ms-credit** (Porta 8081)
- **ms-fraud** (Porta 8083)
- **ms-scoring** (Porta 8082)
- **ms-notification** (Porta 8084)
- **camunda-starter** (Porta 8085)

---

## 🧪 Como Testar (Fluxo E2E)

Para facilitar os testes, disponibilizei uma **Collection do Postman** com todos os cenários prontos (Aprovação, Rejeição por Renda, Blacklist e Rate Limit).

### 1. Importar a Collection
1. No Postman, clique em **Import**.
2. Selecione o arquivo `credit-analysis.postman_collection` localizado na raiz deste projeto.

### 2. Monitoramento em Tempo Real
Antes de disparar as requisições:
1. Abra o arquivo `index.html` no seu navegador.
2. Digite o ID `customer-vip-001` e clique em **Conectar SSE**.

### 3. Execução dos Cenários
Na collection importada, você encontrará os seguintes cenários pré-configurados para validar as regras de negócio:

*   **APROVADO VIP:** Fluxo de sucesso total (Score alto e Renda compatível).
*   **REJEITADO VIP SCORE ALTO:** Demonstra que, mesmo com score excelente, o motor de fraude pode barrar por inconsistência de renda.
*   **REJEITADO RENDA:** Validação de segurança baseada na relação valor solicitado vs. renda declarada.
*   **BLOQUEADO BLOCKLIST:** Validação crítica de segurança consultando o banco de dados PostgreSQL.
*   **ANALISE MANUAL:** Demonstração da `User Task` do Camunda para propostas que exigem verificação humana.
*   **CACHE HIT REDIS:** Teste de performance para validar a persistência e recuperação de scores no Redis.
*   **CACHE RATE LIMIT:** Teste de resiliência para validar o bloqueio de múltiplas requisições (Segurança/Anti-Spam).

### 4. Validação de Auditoria
Após as execuções:
*   **Relatório PDF:** No Monitor HTML, pesquise o histórico do cliente e clique em **📄 Relatório Fraude** para visualizar o laudo técnico gerado pelo `ms-fraud`.
*   **Métricas:** Acesse o Grafana em `http://localhost:3000` para validar os dashboards.

---
![Dashboard Grafana](assets/grafana.png)
---

## 🛡️ Resiliência e Idempotência

- **Idempotência:** O sistema utiliza *Unique Constraints* no banco e verificações no service para evitar duplicidade de laudos em caso de retentativas do Kafka/Camunda.
- **Resiliência:** Implementação de DLQ (Dead Letter Queue) para tratamento de falhas em mensagens críticas.
- **Qualidade:** Cobertura de testes unitários superior a 65% via JaCoCo.

---

## 🔗 Links e Portas Úteis

- **Kafka UI:** [http://localhost:8090](http://localhost:8090)
- **Camunda Cockpit:** [http://localhost:8080](http://localhost:8080)
- **Prometheus:** [http://localhost:9090](http://localhost:9090)
- **Grafana:** [http://localhost:3000](http://localhost:3000)