
Este documento apresenta as principais decisões de segurança adotadas no projeto **API Gateway Seguro para Microserviços**.

--- 

🔑 Autenticação com JWT

A autenticação foi implementada utilizando **JSON Web Token (JWT)**.

O cliente realiza login no Gateway e recebe um token assinado.

JWT foi escolhido porque permite autenticação **stateless**, evitando a necessidade de manter uma sessão de usuário no Gateway.

Além disso, o token pode transportar informações necessárias para autorização, como a role do usuário.

---

👥 Autorização baseada em roles — RBAC

As roles são extraídas dos claims do JWT.


O Gateway transforma a role em uma authority do Spring Security.

As rotas possuem diferentes níveis de acesso:


autenticação e autorização ficam separadas:

```text
JWT válido → usuário autenticado

Role adequada → usuário autorizado
```

---

🔐 BCrypt para armazenamento de senhas

As senhas dos usuários não são armazenadas em texto puro.

Foi utilizado **BCrypt** através do `PasswordEncoder`.

Uma senha armazenada em texto puro poderia ser imediatamente exposta caso o banco de dados fosse comprometido.



🚦 Rate Limiting

O Gateway utiliza **Bucket4j** para controlar a quantidade de requisições.

Configuração:

```text
5 requisições
5 tokens de reposição por minuto
Identificação por IP
```

Quando o limite é ultrapassado:

```http
429 Too Many Requests
```


Rate Limiting reduz o impacto de:

- abuso da API;
- excesso de requisições;
- tentativas automatizadas;
- ataques simples de negação de serviço.


---

🔒 HTTPS

O Gateway está configurado para utilizar HTTPS na porta:

```text
8443
```

JWT contém informações de autenticação e, portanto, não deve ser transmitido em uma conexão HTTP desprotegida.

HTTPS protege a comunicação contra interceptação e alteração dos dados durante o transporte.


---

🛡️ HSTS

Foi configurado o header:

```text
Strict-Transport-Security
```

com:

```text
includeSubdomains=true
max-age=365 dias
```

HSTS instrui clientes compatíveis a utilizarem HTTPS em vez de HTTP para o domínio.

---
🌐 CORS

O Gateway possui configuração explícita de CORS.

São definidos:

- origens permitidas;
- métodos permitidos;
- headers permitidos;
- uso de credenciais;
- tempo de cache da configuração.


CORS impede que qualquer origem realize livremente requisições cross-origin ao Gateway.

---


🧩 Content Security Policy — CSP

Foi configurada uma política CSP semelhante a:

```text
default-src 'self';
script-src 'self';
style-src 'self' 'unsafe-inline';
img-src 'self' data:;
font-src 'self';
connect-src 'self';
frame-ancestors 'none';
```


CSP reduz a possibilidade de execução de conteúdo não autorizado pelo navegador.



---

🖼️ Proteção contra Clickjacking

Foi configurado:

```text
X-Frame-Options: SAMEORIGIN
```


Esse header controla se a aplicação pode ser carregada dentro de um `iframe`.

Isso reduz riscos relacionados a **clickjacking**.


---

🚪 Gateway como ponto único de entrada

O cliente deve acessar:

```text
https://localhost:8443
```

e não diretamente:

```text
http://localhost:8081
```

O Gateway valida o JWT antes do roteamento.


Centralizar a autenticação e autorização reduz a duplicação de lógica de segurança entre microsserviços.

---

🔐 Proteção adicional do Order-Service

O Gateway adiciona:

```http
X-Gateway-Authenticated: true
```

O `Order-Service` verifica esse header.


Essa camada adicional demonstra o conceito de **serviço interno protegido**.

Mesmo que alguém tente acessar diretamente o `Order-Service`, a chamada não possui o header esperado e é rejeitada.

Isso cria uma segunda barreira além da autenticação realizada no Gateway.

---

🧯 Tratamento de erros

O projeto retorna respostas controladas para situações de segurança.


```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid token"
}
```

Para Rate Limiting:

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded"
}
```

Não devem ser expostos ao cliente:

- stack traces;
- nomes de classes internas;
- versões do framework;
- informações de infraestrutura;
- detalhes de banco de dados.

---

🔄 Fluxo de segurança

O fluxo completo é:

```text
1. Cliente realiza login
          ↓
2. Gateway consulta o usuário
          ↓
3. Senha é validada com BCrypt
          ↓
4. Gateway gera JWT
          ↓
5. Cliente envia JWT
          ↓
6. Rate Limiting é aplicado
          ↓
7. JWT é validado
          ↓
8. Role é extraída
          ↓
9. Spring Security verifica autorização
          ↓
10. Gateway encaminha a requisição
          ↓
11. Header X-Gateway-Authenticated é adicionado
          ↓
12. Order-Service valida o header
          ↓
13. Requisição é processada
```

---

