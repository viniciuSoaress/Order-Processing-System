# Order Processing System 🚀

O **Order Processing System** é uma plataforma distribuída de alta performance para o gerenciamento e processamento de pedidos em tempo real. Desenvolvido sob o ecossistema **Spring Boot 3.x**, o projeto adota uma arquitetura multimodular (*Monorepo*) e orientada a eventos (*Event-Driven Architecture*), garantindo isolamento completo de dados, resiliência e alta escalabilidade.

---

## 🛠️ Stack Tecnológica

- **Core:** Java 17 / 21 & Spring Boot 3.x
- **Segurança:** Spring Security 6.x & Auth0 JWT (Bearer Token)
- **Mensageria:** Apache Kafka (Modo KRaft - Sem Zookeeper)
- **Banco de Dados:** PostgreSQL (Padrão *Database per Service*)
- **Containers & Infra:** Docker & Docker Compose
- **Build & Módulos:** Maven (Estrutura Multimodular)

---

## 🏛️ Desenho Arquitetural & Fluxo de Dados

O sistema implementa o padrão **Database per Service**, onde cada microsserviço possui sua própria instância isolada do PostgreSQL, eliminando o acoplamento a nível de persistência. A comunicação síncrona é restrita e a integração principal ocorre de forma assíncrona através de um barramento de eventos.

```text
[ Cliente Final ] ──( Autentica )──> [ pedido-service ] (Gera Token "A" - ROLE_CLIENTE)
         │
         └───( POST /orders ) ──> Grava no Postgres (Pedido)
                                        │
                               ( Kafka Producer ) ──> [ Tópico: order-events ]
                                                             │
                                                     ( Kafka Consumer )
                                                             │
                                                             ▼
[ Equipe Interna ] ──( Autentica )──> [ notificacao-service ] ──> Grava no Postgres (Notificação)
```

---

## 📦 Estrutura de Módulos (Monorepo)

- **`infra-shared/`**: Diretório exclusivo de infraestrutura contendo o cluster local do Apache Kafka em modo KRaft via Docker Compose.
- **`core-shared/`**: Módulo Java centralizado que contém contratos comuns, utilitários globais e o mecanismo estático e agnóstico de validação de assinaturas de criptografia do JWT (`CoreJwtUtil`).
- **`pedido-service/`**: Microsserviço *Producer* responsável pelo ecossistema de vendas, cadastro de clientes, autenticação de usuários finais e publicação de eventos autocontidos no Kafka.
- **`notificacao-service/`**: Microsserviço *Consumer* assíncrono encarregado da ingestão de eventos, persistência do histórico para auditoria e disponibilização de um painel seguro para operadores administrativos.

---

## 🔐 Estratégia de Segurança Dinâmica (JWT Sem State)

Para mitigar o gargalo tradicional de microsserviços consultando bancos de dados ou servidores de identidade a cada requisição HTTP (*I/O Bound*), este projeto implementa um modelo **Stateless Descentralizado com Single Sign-On (SSO) de Escopos Distintos**:

1. **Geração Isolada:** O `pedido-service` (Clientes) e o `notificacao-service` (Operadores) emitem tokens JWT assinados com chaves criptográficas distintas (`HMAC256`).
2. **Claims Empacotadas:** No momento da autenticação, as permissões do usuário do Spring Security são embutidas dentro do token (`.withClaim("roles", ...)`).
3. **Filtro de Performance no Core:** O módulo `core-shared` intercepta as chamadas via `OncePerRequestFilter`. Ele valida a assinatura de forma estática e popula o `SecurityContextHolder` usando apenas os dados extraídos do payload do token, garantindo validação em tempo de CPU, sem latência de rede ou consultas a bancos de dados externos.

---

## 📨 Contrato de Evento Autocontido (`OrderCreatedEvent`)

A comunicação entre os serviços utiliza mensagens altamente estruturadas e autocontidas, permitindo que o consumidor processe a informação de ponta a ponta sem necessidade de realizar chamadas REST de volta ao serviço de origem.

```json
{
  "eventId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "createdAt": "2026-11-04T21:07:00Z",
  "orderId": 1024,
  "customerId": "client-9982",
  "totalAmount": 250.00,
  "status": "CREATED",
  "items": [
    { "productId": "prod-123", "quantity": 2, "price": 100.00 },
    { "productId": "prod-456", "quantity": 1, "price": 50.00 }
  ]
}
```

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
- Java 17 ou superior instalado
- Docker e Docker Compose ativos

### Passo 1: Subir a Mensageria (Kafka KRaft)
```bash
cd infra-shared
docker compose up -d
```

### Passo 2: Subir os Bancos de Dados Isolados
Navegue até as pastas `pedido-service` e `notificacao-service` e execute os respectivos comandos Docker Compose locais para erguer as instâncias do PostgreSQL.

### Passo 3: Compilar e Executar
Execute o build do projeto Maven/Gradle na raiz e inicialize as aplicações Spring Boot. Graças à dependência `spring-boot-docker-compose`, os serviços se integrarão perfeitamente com os containers locais ativos.
