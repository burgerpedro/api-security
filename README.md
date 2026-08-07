# 🔐 API Gateway Seguro para Microserviços

Projeto final de segurança de APIs desenvolvido com **Java 21, Spring Boot, Spring Security e Spring Cloud Gateway**.

O projeto simula uma arquitetura corporativa na qual o **API Gateway é o ponto único de entrada**. O Gateway autentica e autoriza as requisições, aplica controles de segurança e encaminha as chamadas válidas para o `Order-Service`.

---

## 📐 Arquitetura

```text
                         CLIENTE
                            │
                            │ HTTPS :8443
                            ▼
                 ┌──────────────────────┐
                 │     API GATEWAY      │
                 │──────────────────────│
                 │ JWT Authentication   │
                 │ RBAC                 │
                 │ Rate Limiting        │
                 │ CORS                 │
                 │ CSRF                 │
                 │ HTTPS / HSTS         │
                 │ CSP / Security       │
                 │ Headers              │
                 │ Tratamento de erros  │
                 └──────────┬───────────┘
                            │
                            │ HTTP interno :8081
                            │ X-Gateway-Authenticated: true
                            ▼
                 ┌──────────────────────┐
                 │    ORDER-SERVICE     │
                 │──────────────────────│
                 │ Valida Gateway       │
                 │ Endpoint de pedidos  │
                 └──────────────────────┘
```

### Componentes

| Componente | Porta | Função |
|---|---:|---|
| API Gateway | `8443` | Entrada externa e segurança |
| Order-Service | `8081` | Microsserviço interno |

---

## 🛠️ Tecnologias

- Java 21
- Spring Boot
- Spring Security
- Spring Cloud Gateway
- Spring WebFlux
- Spring Data R2DBC
- H2 Database
- JJWT
- Bucket4j
- Maven
- Insomnia

---

## ▶️ Como executar

### 1. Iniciar o Order-Service

O serviço deve estar disponível em:

```text
http://localhost:8081
```

### 2. Iniciar o API Gateway

O Gateway deve estar disponível em:

```text
https://localhost:8443
```

O projeto utiliza um certificado local `keystore.p12`.

Gerador para certificado auto assinado dentro do projeto gerar-certificado.bat


---

# 🔐 Segurança

O API Gateway centraliza os principais controles de segurança da aplicação:

- 🔑 Autenticação e validação de JWT
- 👥 Autorização baseada em roles (RBAC)
- 🔒 HTTPS e HSTS
- 🚦 Rate Limiting
- 🌐 CORS
- 🛡️ CSRF
- 🧱 Content Security Policy (CSP)
- 🔐 BCrypt para armazenamento de senhas
- 🧯 Tratamento seguro de erros

---

# 🔑 Autenticação

O login é realizado pelo próprio Gateway.

### Endpoint

```http
POST https://localhost:8443/api/auth/login
```

### Body

```json
{
  "username": "admin",
  "password": "senha123"
}
```

### Resposta

```json
{
  "token": "JWT..."
}
```

O JWT retornado deve ser enviado nas rotas protegidas:

```http
Authorization: Bearer <TOKEN>
```

---

## 👤 Usuários de teste

| Usuário | Senha | Role |
|---|---|---|
| `admin` | `senha123` | `ADMIN` |
| `user` | `senha123` | `USER` |

As senhas são armazenadas utilizando **BCrypt**.


---

# 🛡️ Rotas protegidas

## Pedidos

```http
GET https://localhost:8443/api/pedidos
```

Permite:

- `USER`
- `ADMIN`

---

## Pedidos administrativos

```http
GET https://localhost:8443/api/pedidos/admin
```

Permite somente:

- `ADMIN`

---

## 📋 Comportamento esperado

| Cenário | Resultado |
|---|---:|
| `/api/pedidos` sem JWT | `401 Unauthorized` |
| `/api/pedidos` com USER | `200 OK` |
| `/api/pedidos` com ADMIN | `200 OK` |
| `/api/pedidos/admin` com USER | `403 Forbidden` |
| `/api/pedidos/admin` com ADMIN | `200 OK` |
| JWT inválido | `401 Unauthorized` |
| JWT expirado | `401 Unauthorized` |
| Excesso de requisições | `429 Too Many Requests` |

---

# 🚦 Rate Limiting

O Gateway utiliza **Bucket4j** para limitar requisições.

Configuração atual:

```text
Capacidade: 5 requisições
Reposição: 5 tokens por minuto
Identificação: endereço IP
```

Ao exceder o limite:

```http
429 Too Many Requests
```

Resposta:

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded"
}
```

---

# 🔒 Proteção do Order-Service

O Gateway adiciona:

```http
X-Gateway-Authenticated: true
```

O `Order-Service` verifica esse cabeçalho antes de processar a requisição.

Uma chamada direta:

```http
GET http://localhost:8081/api/pedidos
```

sem o cabeçalho deve resultar em:

```http
401 Unauthorized
```

Isso demonstra que o microsserviço interno não deve aceitar chamadas que não passaram pelo Gateway.

---

# 🌐 CORS

O Gateway possui configuração de CORS para controlar requisições cross-origin.

São permitidos:

```text
GET
POST
PUT
DELETE
OPTIONS
```

Headers permitidos:

```text
Authorization
Content-Type
X-Requested-With
```

Como o projeto não possui frontend, a configuração existe principalmente para demonstrar o controle de CORS exigido no projeto.

---

# 🔐 HTTPS

O Gateway utiliza HTTPS:

```text
https://localhost:8443
```

Também está configurado **HSTS**, reforçando o uso de comunicação segura.

---

# 🧪 Testes com Insomnia

A coleção:

```text
insomnia-api-gateway.json
```

contém os principais cenários de demonstração:

1. Login ADMIN
2. Login USER
3. Acesso autorizado aos pedidos
4. Acesso administrativo
5. Requisição sem JWT
6. JWT inválido
7. JWT expirado
8. Acesso direto ao Order-Service
9. Teste de Rate Limiting
10. Excesso de requisições → `429`

Após realizar o login, copie o JWT retornado para a variável:

```text
token
```

do ambiente do Insomnia.


