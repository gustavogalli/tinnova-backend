# 🚗 Tinnova – API REST de Gestão de Veículos

API REST desenvolvida como desafio técnico, com foco em boas práticas de arquitetura, segurança, cache, documentação e testes.  
O sistema permite o gerenciamento de veículos, com controle de acesso baseado em papéis (ADMIN / USER) e conversão de valores USD → BRL utilizando cache Redis.

---

## 🎯 Objetivo do Projeto

O objetivo deste desafio é avaliar a capacidade de projetar, implementar e testar uma API REST com requisitos de negócio, segurança e qualidade de código.  
A API é responsável por gerenciar veículos, permitindo operações de consulta, cadastro, atualização e remoção, com controle de acesso baseado em papéis.

---

## 🛠️ Tecnologias Utilizadas

- Java 17
- Spring Boot 3.2.5
- Spring Web
- Spring Data JPA
- Spring Security + JWT
- Spring Cache + Redis
- H2 Database (em memória)
- Swagger / OpenAPI (Springdoc)
- Docker (Redis)
- Lombok
- JUnit / Spring Security Test

---

## 🧱 Arquitetura e Decisões Técnicas

- Autenticação Stateless com JWT
- Controle de acesso por papel (ROLE_ADMIN / ROLE_USER)
- Cache Redis para cotação USD → BRL
- Fallback de API externa:
    - Primeira tentativa: AwesomeAPI
    - Fallback automático: Frankfurter API
- Banco H2 em memória para facilitar testes locais
- Documentação automática via OpenAPI
- Separação clara de responsabilidades:
    - Controller
    - Service
    - Mapper
    - Repository
    - Configuração

---

## 🔐 Segurança

### Usuários em memória

| Usuário | Senha  | Role  |
|---------|-------|-------|
| admin   | admin | ADMIN |
| user    | 1234  | USER  |

### Regras de acesso

**ADMIN**

- Criar veículo (POST)
- Atualizar veículo (PUT / PATCH)
- Remover veículo (DELETE)

**USER**

- Consultar veículos (GET)

### Endpoints públicos

- `/auth/**`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/h2-console/**`

---

## 🔑 Autenticação (JWT)

### Login

**POST** `/auth/login`

```json
{
  "username": "admin",
  "password": "admin"
}
```
Resposta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```
#### Uso do token
Enviar em todas as requsições protegidas:

```
Authorization: Bearer <TOKEN>
```
---

## 📚 Documentação da API (Swagger)

- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)
- **OpenAPI (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

No Swagger:

1. Clique em **Authorize**
2. Informe: `Bearer <TOKEN>`
3. Teste os endpoints normalmente

---

## 🚀 Como Executar o Projeto

### Pré-requisitos

- Java 17+
- Maven
- Docker (para Redis)

### 🔴 Subindo o Redis

```bash
docker run -d -p 6379:6379 redis
```
Ou verifique se já está rodando:
```
docker ps
```

### ▶️ Subindo a aplicação
```
mvn clean install
mvn spring-boot:run
```

Aplicação disponível em: http://localhost:8080

---

### 🧠 Cache Redis (USD → BRL)
- Cache configurado com TTL de 10 minutos
- Key utilizada: usd-brl
- A primeira chamada busca da API externa
- Chamadas seguintes utilizam Redis
- Funciona mesmo sem internet, enquanto o cache for válido

Logs ajudam a visualizar:

```
logging:
level:
org.springframework.cache: TRACE
org.springframework.data.redis: DEBUG
```

--- 

### 🗄️ Banco de Dados (H2)
- Banco em memória
- Console disponível em: http://localhost:8080/h2-console

Configurações:
- JDBC URL: jdbc:h2:mem:tinnova-db
- User: sa
- Password: (em branco)

---
## 📌 Principais Endpoints

### Veículos

| Método | Endpoint                       | Descrição                   | Role  |
|--------|--------------------------------|-----------------------------|-------|
| GET    | /veiculos                      | Listar veículos com filtros | USER  |
| GET    | /veiculos/{id}                 | Detalhar veículo            | USER  |
| POST   | /veiculos                      | Criar veículo               | ADMIN |
| PUT    | /veiculos/{id}                 | Atualizar veículo           | ADMIN |
| PATCH  | /veiculos/{id}                 | Atualização parcial         | ADMIN |
| DELETE | /veiculos/{id}                 | Remover veículo             | ADMIN |
| GET    | /veiculos/relatorios/por-marca | Relatório por marca         | USER  |

---

## 🧪 Testes

- Testes unitários com JUnit
- Testes de segurança com Spring Security Test
- Validações automáticas via Bean Validation
- Testes manuais facilitados via Swagger

---

## 📎 Considerações Finais

Este projeto foi desenvolvido com foco em:

- Clareza de código
- Organização
- Boas práticas REST
- Segurança moderna (JWT)
- Performance com cache Redis
- Documentação clara para consumo da API
